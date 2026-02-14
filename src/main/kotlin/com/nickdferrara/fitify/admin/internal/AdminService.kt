package com.nickdferrara.fitify.admin.internal

import com.nickdferrara.fitify.admin.AdminApi
import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import com.nickdferrara.fitify.admin.internal.dtos.request.CreateClassRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.CreateRecurringScheduleRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.UpdateBusinessRuleRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.UpdateClassRequest
import com.nickdferrara.fitify.admin.internal.dtos.response.AdminClassResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.BusinessRuleResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.CancelClassResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.RecurringScheduleResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.toAdminResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.toResponse
import com.nickdferrara.fitify.admin.internal.entities.BusinessRule
import com.nickdferrara.fitify.admin.internal.entities.RecurringSchedule
import com.nickdferrara.fitify.admin.internal.exceptions.BusinessRuleNotFoundException
import com.nickdferrara.fitify.admin.internal.exceptions.ClassNotFoundException
import com.nickdferrara.fitify.admin.internal.exceptions.CoachNotFoundException
import com.nickdferrara.fitify.admin.internal.exceptions.CoachScheduleConflictException
import com.nickdferrara.fitify.admin.internal.exceptions.InvalidRecurringScheduleException
import com.nickdferrara.fitify.admin.internal.exceptions.LocationNotFoundException
import com.nickdferrara.fitify.admin.internal.repository.BusinessRuleRepository
import com.nickdferrara.fitify.admin.internal.repository.RecurringScheduleRepository
import com.nickdferrara.fitify.coaching.CoachingApi
import com.nickdferrara.fitify.location.LocationApi
import com.nickdferrara.fitify.scheduling.CreateClassCommand
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.scheduling.UpdateClassCommand
import com.nickdferrara.fitify.shared.Result
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.ZoneId
import java.util.UUID

@Service
internal class AdminService(
    private val schedulingApi: SchedulingApi,
    private val coachingApi: CoachingApi,
    private val locationApi: LocationApi,
    private val recurringScheduleRepository: RecurringScheduleRepository,
    private val businessRuleRepository: BusinessRuleRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : AdminApi {

    fun createClass(locationId: UUID, request: CreateClassRequest): AdminClassResponse {
        validateLocationExists(locationId)
        validateCoachExistsAndActive(request.coachId)
        checkCoachConflict(request.coachId, request.startTime, request.endTime, excludeClassId = null)

        val command = CreateClassCommand(
            name = request.name,
            description = request.description,
            classType = request.classType,
            coachId = request.coachId,
            room = request.room,
            startTime = request.startTime,
            endTime = request.endTime,
            capacity = request.capacity,
        )

        return schedulingApi.createClass(locationId, command).toAdminResponse()
    }

    fun updateClass(classId: UUID, request: UpdateClassRequest): AdminClassResponse {
        val existing = when (val result = schedulingApi.getClassDetail(classId)) {
            is Result.Success -> result.value
            is Result.Failure -> throw ClassNotFoundException(classId)
        }

        if (request.coachId != null || request.startTime != null || request.endTime != null) {
            val coachId = request.coachId ?: existing.coachId
            val startTime = request.startTime ?: existing.startTime
            val endTime = request.endTime ?: existing.endTime

            if (request.coachId != null && request.coachId != existing.coachId) {
                validateCoachExistsAndActive(coachId)
            }
            checkCoachConflict(coachId, startTime, endTime, excludeClassId = classId)
        }

        val command = UpdateClassCommand(
            name = request.name,
            description = request.description,
            classType = request.classType,
            coachId = request.coachId,
            room = request.room,
            startTime = request.startTime,
            endTime = request.endTime,
            capacity = request.capacity,
        )

        return schedulingApi.updateClass(classId, command).toAdminResponse()
    }

    fun cancelClass(classId: UUID): CancelClassResponse {
        val result = schedulingApi.cancelClass(classId)
        return CancelClassResponse(
            classId = result.classId,
            className = result.className,
            affectedBookings = result.affectedUserIds.size,
            affectedWaitlist = result.waitlistUserIds.size,
        )
    }

    fun listClassesByLocation(locationId: UUID): List<AdminClassResponse> {
        validateLocationExists(locationId)
        return schedulingApi.findClassesByLocationId(locationId).map { it.toAdminResponse() }
    }

    @Transactional
    fun createRecurringSchedule(
        locationId: UUID,
        request: CreateRecurringScheduleRequest,
    ): RecurringScheduleResponse {
        if (request.endDate.isBefore(request.startDate)) {
            throw InvalidRecurringScheduleException("End date must be after start date")
        }
        if (request.daysOfWeek.isEmpty()) {
            throw InvalidRecurringScheduleException("At least one day of week is required")
        }

        val location = when (val result = locationApi.findLocationById(locationId)) {
            is Result.Success -> result.value
            is Result.Failure -> throw LocationNotFoundException(locationId)
        }

        validateCoachExistsAndActive(request.coachId)

        val schedule = RecurringSchedule(
            locationId = locationId,
            name = request.name,
            description = request.description,
            classType = request.classType,
            coachId = request.coachId,
            room = request.room,
            daysOfWeek = request.daysOfWeek,
            startTime = request.startTime,
            durationMinutes = request.durationMinutes,
            capacity = request.capacity,
            startDate = request.startDate,
            endDate = request.endDate,
        )
        val savedSchedule = recurringScheduleRepository.save(schedule)

        val timeZone = ZoneId.of(location.timeZone)
        val targetDays = request.daysOfWeek.map { DayOfWeek.valueOf(it.uppercase()) }.toSet()
        var classesCreated = 0

        var currentDate = request.startDate
        while (!currentDate.isAfter(request.endDate)) {
            if (currentDate.dayOfWeek in targetDays) {
                val zonedStart = currentDate.atTime(request.startTime).atZone(timeZone)
                val zonedEnd = zonedStart.plusMinutes(request.durationMinutes.toLong())
                val startInstant = zonedStart.toInstant()
                val endInstant = zonedEnd.toInstant()

                val conflicts = schedulingApi.findClassesByCoachIdAndTimeRange(
                    request.coachId, startInstant, endInstant,
                )

                if (conflicts.isEmpty()) {
                    val command = CreateClassCommand(
                        name = request.name,
                        description = request.description,
                        classType = request.classType,
                        coachId = request.coachId,
                        room = request.room,
                        startTime = startInstant,
                        endTime = endInstant,
                        capacity = request.capacity,
                    )
                    schedulingApi.createClass(locationId, command)
                    classesCreated++
                }
            }
            currentDate = currentDate.plusDays(1)
        }

        return RecurringScheduleResponse(
            id = savedSchedule.id!!,
            locationId = savedSchedule.locationId,
            name = savedSchedule.name,
            description = savedSchedule.description,
            classType = savedSchedule.classType,
            coachId = savedSchedule.coachId,
            room = savedSchedule.room,
            daysOfWeek = savedSchedule.daysOfWeek,
            startTime = savedSchedule.startTime,
            durationMinutes = savedSchedule.durationMinutes,
            capacity = savedSchedule.capacity,
            startDate = savedSchedule.startDate,
            endDate = savedSchedule.endDate,
            classesCreated = classesCreated,
        )
    }

    // --- Business Rule methods ---

    override fun getBusinessRuleValue(ruleKey: String, locationId: UUID?): String? {
        if (locationId != null) {
            val override = businessRuleRepository.findByRuleKeyAndLocationId(ruleKey, locationId)
            if (override != null) return override.value
        }
        return businessRuleRepository.findByRuleKeyAndLocationIdIsNull(ruleKey)?.value
    }

    fun listBusinessRules(): List<BusinessRuleResponse> {
        return businessRuleRepository.findAllByOrderByRuleKeyAscLocationIdAsc().map { it.toResponse() }
    }

    @Transactional
    fun updateBusinessRule(ruleKey: String, request: UpdateBusinessRuleRequest, updatedBy: String): BusinessRuleResponse {
        val existing = if (request.locationId != null) {
            businessRuleRepository.findByRuleKeyAndLocationId(ruleKey, request.locationId)
        } else {
            businessRuleRepository.findByRuleKeyAndLocationIdIsNull(ruleKey)
        }

        val saved = if (existing != null) {
            existing.value = request.value
            existing.description = request.description ?: existing.description
            existing.updatedBy = updatedBy
            businessRuleRepository.save(existing)
        } else {
            val globalRule = businessRuleRepository.findByRuleKeyAndLocationIdIsNull(ruleKey)
                ?: throw BusinessRuleNotFoundException(ruleKey)

            if (request.locationId == null) {
                throw BusinessRuleNotFoundException(ruleKey)
            }

            businessRuleRepository.save(
                BusinessRule(
                    ruleKey = ruleKey,
                    value = request.value,
                    locationId = request.locationId,
                    description = request.description ?: globalRule.description,
                    updatedBy = updatedBy,
                ),
            )
        }

        eventPublisher.publishEvent(
            BusinessRuleUpdatedEvent(
                ruleKey = ruleKey,
                newValue = request.value,
                locationId = request.locationId,
                updatedBy = updatedBy,
            ),
        )

        return saved.toResponse()
    }

    // --- Private validation helpers ---

    private fun validateLocationExists(locationId: UUID) {
        when (locationApi.findLocationById(locationId)) {
            is Result.Success -> {}
            is Result.Failure -> throw LocationNotFoundException(locationId)
        }
    }

    private fun validateCoachExistsAndActive(coachId: UUID) {
        when (val result = coachingApi.findCoachById(coachId)) {
            is Result.Success -> {
                if (!result.value.active) {
                    throw CoachNotFoundException(coachId)
                }
            }
            is Result.Failure -> throw CoachNotFoundException(coachId)
        }
    }

    private fun checkCoachConflict(
        coachId: UUID,
        startTime: java.time.Instant,
        endTime: java.time.Instant,
        excludeClassId: UUID?,
    ) {
        val conflicts = schedulingApi.findClassesByCoachIdAndTimeRange(coachId, startTime, endTime)
        val actualConflicts = if (excludeClassId != null) {
            conflicts.filter { it.id != excludeClassId }
        } else {
            conflicts
        }
        if (actualConflicts.isNotEmpty()) {
            throw CoachScheduleConflictException(
                coachId,
                "Overlapping with class '${actualConflicts.first().name}' " +
                    "(${actualConflicts.first().startTime} - ${actualConflicts.first().endTime})",
            )
        }
    }
}
