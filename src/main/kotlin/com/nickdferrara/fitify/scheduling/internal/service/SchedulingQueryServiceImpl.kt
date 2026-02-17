package com.nickdferrara.fitify.scheduling.internal.service

import com.nickdferrara.fitify.coaching.CoachAssignedEvent
import com.nickdferrara.fitify.scheduling.CancelClassResult
import com.nickdferrara.fitify.scheduling.ClassCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassDetail
import com.nickdferrara.fitify.scheduling.ClassSummary
import com.nickdferrara.fitify.scheduling.ClassUpdatedEvent
import com.nickdferrara.fitify.scheduling.ClassUtilizationSummary
import com.nickdferrara.fitify.scheduling.CreateClassCommand
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.scheduling.UpdateClassCommand
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import com.nickdferrara.fitify.scheduling.internal.enums.BookingStatus
import com.nickdferrara.fitify.scheduling.internal.enums.FitnessClassStatus
import com.nickdferrara.fitify.scheduling.internal.exceptions.FitnessClassNotFoundException
import com.nickdferrara.fitify.scheduling.internal.extensions.toDetail
import com.nickdferrara.fitify.scheduling.internal.extensions.toSummary
import com.nickdferrara.fitify.scheduling.internal.repository.BookingRepository
import com.nickdferrara.fitify.scheduling.internal.repository.FitnessClassRepository
import com.nickdferrara.fitify.scheduling.internal.repository.WaitlistEntryRepository
import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
internal class SchedulingQueryServiceImpl(
    private val fitnessClassRepository: FitnessClassRepository,
    private val bookingRepository: BookingRepository,
    private val waitlistEntryRepository: WaitlistEntryRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : SchedulingApi {

    override fun findClassById(id: UUID): Result<ClassSummary, DomainError> {
        val fitnessClass = fitnessClassRepository.findById(id).orElse(null)
            ?: return Result.Failure(NotFoundError("Class not found: $id"))
        return Result.Success(fitnessClass.toSummary())
    }

    override fun findUpcomingClassesByLocationId(locationId: UUID): List<ClassSummary> {
        return fitnessClassRepository
            .findByLocationIdAndStartTimeAfterOrderByStartTimeAsc(locationId, Instant.now())
            .map { it.toSummary() }
    }

    @Transactional
    override fun createClass(locationId: UUID, command: CreateClassCommand): ClassDetail {
        val fitnessClass = FitnessClass(
            locationId = locationId,
            name = command.name,
            description = command.description,
            classType = command.classType,
            coachId = command.coachId,
            room = command.room,
            startTime = command.startTime,
            endTime = command.endTime,
            capacity = command.capacity,
        )
        return fitnessClassRepository.save(fitnessClass).toDetail()
    }

    @Transactional
    override fun updateClass(classId: UUID, command: UpdateClassCommand): ClassDetail {
        val fitnessClass = fitnessClassRepository.findById(classId)
            .orElseThrow { FitnessClassNotFoundException(classId) }

        val updatedFields = mutableListOf<String>()

        command.name?.let { fitnessClass.name = it; updatedFields.add("name") }
        command.description?.let { fitnessClass.description = it; updatedFields.add("description") }
        command.classType?.let { fitnessClass.classType = it; updatedFields.add("classType") }
        command.coachId?.let { fitnessClass.coachId = it; updatedFields.add("coachId") }
        command.room?.let { fitnessClass.room = it; updatedFields.add("room") }
        command.startTime?.let { fitnessClass.startTime = it; updatedFields.add("startTime") }
        command.endTime?.let { fitnessClass.endTime = it; updatedFields.add("endTime") }
        command.capacity?.let { fitnessClass.capacity = it; updatedFields.add("capacity") }

        val saved = fitnessClassRepository.save(fitnessClass)

        if ("coachId" in updatedFields && command.coachId != null) {
            eventPublisher.publishEvent(
                CoachAssignedEvent(
                    coachId = command.coachId!!,
                    classId = classId,
                )
            )
        }

        if (updatedFields.any { it in listOf("startTime", "endTime", "capacity") }) {
            val affectedUserIds = bookingRepository
                .findByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED)
                .map { it.userId }

            eventPublisher.publishEvent(
                ClassUpdatedEvent(
                    classId = classId,
                    className = fitnessClass.name,
                    locationId = fitnessClass.locationId,
                    updatedFields = updatedFields,
                    affectedUserIds = affectedUserIds,
                )
            )
        }

        return saved.toDetail()
    }

    @Transactional
    override fun cancelClass(classId: UUID): CancelClassResult {
        val fitnessClass = fitnessClassRepository.findById(classId)
            .orElseThrow { FitnessClassNotFoundException(classId) }

        fitnessClass.status = FitnessClassStatus.CANCELLED

        val confirmedBookings = bookingRepository
            .findByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED)
        val affectedUserIds = confirmedBookings.map { it.userId }
        confirmedBookings.forEach { booking ->
            booking.status = BookingStatus.CANCELLED
            booking.cancelledAt = Instant.now()
            bookingRepository.save(booking)
        }

        val waitlistEntries = waitlistEntryRepository
            .findByFitnessClassIdOrderByPositionAsc(classId)
        val waitlistUserIds = waitlistEntries.map { it.userId }
        waitlistEntryRepository.deleteAll(waitlistEntries)

        fitnessClassRepository.save(fitnessClass)

        eventPublisher.publishEvent(
            ClassCancelledEvent(
                classId = classId,
                className = fitnessClass.name,
                locationId = fitnessClass.locationId,
                originalStartTime = fitnessClass.startTime,
                affectedUserIds = affectedUserIds,
                waitlistUserIds = waitlistUserIds,
                cancelledAt = Instant.now(),
            )
        )

        return CancelClassResult(
            classId = classId,
            className = fitnessClass.name,
            affectedUserIds = affectedUserIds,
            waitlistUserIds = waitlistUserIds,
        )
    }

    override fun getClassDetail(classId: UUID): Result<ClassDetail, DomainError> {
        val fitnessClass = fitnessClassRepository.findById(classId).orElse(null)
            ?: return Result.Failure(NotFoundError("Class not found: $classId"))
        return Result.Success(fitnessClass.toDetail())
    }

    override fun findClassesByLocationId(locationId: UUID): List<ClassDetail> {
        return fitnessClassRepository
            .findByLocationIdAndStartTimeAfterOrderByStartTimeAsc(locationId, Instant.now())
            .map { it.toDetail() }
    }

    override fun findClassesByCoachIdAndTimeRange(
        coachId: UUID,
        startTime: Instant,
        endTime: Instant,
    ): List<ClassSummary> {
        return fitnessClassRepository
            .findByCoachIdAndTimeRange(coachId, startTime, endTime)
            .map { it.toSummary() }
    }

    override fun getClassUtilizationByDateRange(start: Instant, end: Instant): List<ClassUtilizationSummary> {
        return fitnessClassRepository.findWithBookingsByDateRange(start, end).map { fc ->
            ClassUtilizationSummary(
                classId = fc.id!!,
                locationId = fc.locationId,
                classType = fc.classType,
                capacity = fc.capacity,
                enrolledCount = fc.bookings.count { it.status == BookingStatus.CONFIRMED },
            )
        }
    }

    override fun countBookingCancellationsBetween(start: Instant, end: Instant, locationId: UUID?): Long {
        return if (locationId != null) {
            bookingRepository.countCancellationsBetweenAndLocationId(start, end, locationId)
        } else {
            bookingRepository.countCancellationsBetween(start, end)
        }
    }
}
