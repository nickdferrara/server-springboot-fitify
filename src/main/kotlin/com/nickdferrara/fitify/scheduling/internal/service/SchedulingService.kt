package com.nickdferrara.fitify.scheduling.internal.service

import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.CancelClassResult
import com.nickdferrara.fitify.scheduling.ClassBookedEvent
import com.nickdferrara.fitify.scheduling.ClassCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassDetail
import com.nickdferrara.fitify.scheduling.ClassFullEvent
import com.nickdferrara.fitify.scheduling.ClassSummary
import com.nickdferrara.fitify.scheduling.ClassUpdatedEvent
import com.nickdferrara.fitify.scheduling.ClassUtilizationSummary
import com.nickdferrara.fitify.scheduling.CreateClassCommand
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.scheduling.UpdateClassCommand
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.scheduling.internal.dtos.request.CreateClassRequest
import com.nickdferrara.fitify.scheduling.internal.dtos.request.UpdateClassRequest
import com.nickdferrara.fitify.scheduling.internal.dtos.response.BookingResponse
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ClassResponse
import com.nickdferrara.fitify.scheduling.internal.dtos.response.WaitlistEntryResponse
import com.nickdferrara.fitify.scheduling.internal.dtos.response.toResponse
import com.nickdferrara.fitify.scheduling.internal.entities.Booking
import com.nickdferrara.fitify.scheduling.internal.entities.BookingStatus
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClassStatus
import com.nickdferrara.fitify.scheduling.internal.entities.WaitlistEntry
import com.nickdferrara.fitify.scheduling.internal.repository.BookingRepository
import com.nickdferrara.fitify.scheduling.internal.repository.FitnessClassRepository
import com.nickdferrara.fitify.scheduling.internal.repository.FitnessClassSpecifications
import com.nickdferrara.fitify.scheduling.internal.repository.WaitlistEntryRepository
import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@Service
internal class SchedulingService(
    private val fitnessClassRepository: FitnessClassRepository,
    private val bookingRepository: BookingRepository,
    private val waitlistEntryRepository: WaitlistEntryRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : SchedulingApi {

    var cancellationWindowHours: Long = 24
    var maxWaitlistSize: Int = 20
    var maxBookingsPerDay: Int = 3

    // --- Public API (cross-module) ---

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

    // --- Search ---

    fun searchClasses(
        date: LocalDate?,
        classType: String?,
        coachId: UUID?,
        locationId: UUID?,
        available: Boolean?,
        pageable: Pageable,
    ): Page<ClassResponse> {
        val spec = FitnessClassSpecifications.combine(
            FitnessClassSpecifications.isActive(),
            FitnessClassSpecifications.isFuture(),
            date?.let { FitnessClassSpecifications.hasDate(it) },
            classType?.let { FitnessClassSpecifications.hasClassType(it) },
            coachId?.let { FitnessClassSpecifications.hasCoach(it) },
            locationId?.let { FitnessClassSpecifications.hasLocation(it) },
            if (available == true) FitnessClassSpecifications.hasAvailability() else null,
        )
        return fitnessClassRepository.findAll(spec, pageable).map { it.toResponse() }
    }

    // --- Internal CRUD (used by internal controllers) ---

    @Transactional
    fun createClass(locationId: UUID, request: CreateClassRequest): ClassResponse {
        val fitnessClass = FitnessClass(
            locationId = locationId,
            name = request.name,
            description = request.description,
            classType = request.classType,
            coachId = request.coachId,
            room = request.room,
            startTime = request.startTime,
            endTime = request.endTime,
            capacity = request.capacity,
        )
        return fitnessClassRepository.save(fitnessClass).toResponse()
    }

    fun getClass(classId: UUID): ClassResponse {
        val fitnessClass = fitnessClassRepository.findById(classId)
            .orElseThrow { FitnessClassNotFoundException(classId) }
        return fitnessClass.toResponse()
    }

    @Transactional
    fun updateClass(classId: UUID, request: UpdateClassRequest): ClassResponse {
        val fitnessClass = fitnessClassRepository.findById(classId)
            .orElseThrow { FitnessClassNotFoundException(classId) }

        request.name?.let { fitnessClass.name = it }
        request.description?.let { fitnessClass.description = it }
        request.classType?.let { fitnessClass.classType = it }
        request.coachId?.let { fitnessClass.coachId = it }
        request.room?.let { fitnessClass.room = it }
        request.startTime?.let { fitnessClass.startTime = it }
        request.endTime?.let { fitnessClass.endTime = it }
        request.capacity?.let { fitnessClass.capacity = it }

        return fitnessClassRepository.save(fitnessClass).toResponse()
    }

    @Transactional
    fun cancelClassInternal(classId: UUID) {
        val fitnessClass = fitnessClassRepository.findById(classId)
            .orElseThrow { FitnessClassNotFoundException(classId) }

        fitnessClass.status = FitnessClassStatus.CANCELLED

        val confirmedBookings = bookingRepository
            .findByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED)
        confirmedBookings.forEach { booking ->
            booking.status = BookingStatus.CANCELLED
            booking.cancelledAt = Instant.now()
            bookingRepository.save(booking)
        }

        val waitlistEntries = waitlistEntryRepository
            .findByFitnessClassIdOrderByPositionAsc(classId)
        waitlistEntryRepository.deleteAll(waitlistEntries)

        fitnessClassRepository.save(fitnessClass)
    }

    // --- Booking ---

    @Transactional
    fun bookClass(classId: UUID, userId: UUID): BookClassResult {
        val fitnessClass = fitnessClassRepository.findById(classId)
            .orElseThrow { FitnessClassNotFoundException(classId) }

        if (fitnessClass.status != FitnessClassStatus.ACTIVE) {
            throw ClassNotBookableException(classId, "Class is not active")
        }
        if (fitnessClass.startTime.isBefore(Instant.now())) {
            throw ClassNotBookableException(classId, "Class has already started")
        }

        val existingBooking = bookingRepository
            .findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED)
        if (existingBooking != null) {
            throw AlreadyBookedException(classId, userId)
        }

        val overlapping = bookingRepository
            .findOverlappingBookings(userId, fitnessClass.startTime, fitnessClass.endTime)
        if (overlapping.isNotEmpty()) {
            throw ScheduleConflictException(userId, fitnessClass.startTime, fitnessClass.endTime)
        }

        val dayStart = fitnessClass.startTime.atZone(ZoneOffset.UTC).toLocalDate()
            .atStartOfDay().toInstant(ZoneOffset.UTC)
        val dayEnd = dayStart.atZone(ZoneOffset.UTC).toLocalDate()
            .plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val dailyCount = bookingRepository.countUserBookingsForDay(userId, dayStart, dayEnd)
        if (dailyCount >= maxBookingsPerDay) {
            throw DailyBookingLimitExceededException(userId, maxBookingsPerDay)
        }

        // TODO: Check subscription status when subscription module is implemented

        val confirmedCount = bookingRepository
            .countByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED)

        if (confirmedCount < fitnessClass.capacity) {
            val booking = Booking(
                userId = userId,
                fitnessClass = fitnessClass,
            )
            val saved = bookingRepository.save(booking)

            eventPublisher.publishEvent(
                ClassBookedEvent(
                    userId = userId,
                    classId = classId,
                    className = fitnessClass.name,
                    startTime = fitnessClass.startTime,
                )
            )

            return BookClassResult.Booked(saved.toResponse())
        }

        val waitlistCount = waitlistEntryRepository.countByFitnessClassId(classId)
        if (waitlistCount >= maxWaitlistSize) {
            throw WaitlistFullException(classId)
        }

        val existingWaitlist = waitlistEntryRepository
            .findByFitnessClassIdAndUserId(classId, userId)
        if (existingWaitlist != null) {
            throw AlreadyBookedException(classId, userId)
        }

        val entry = WaitlistEntry(
            userId = userId,
            fitnessClass = fitnessClass,
            position = (waitlistCount + 1).toInt(),
        )
        val savedEntry = waitlistEntryRepository.save(entry)

        eventPublisher.publishEvent(
            ClassFullEvent(
                classId = classId,
                className = fitnessClass.name,
                waitlistSize = (waitlistCount + 1).toInt(),
            )
        )

        return BookClassResult.Waitlisted(savedEntry.toResponse())
    }

    @Transactional
    fun cancelBooking(classId: UUID, userId: UUID) {
        val booking = bookingRepository
            .findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED)
            ?: throw BookingNotFoundException(classId, userId)

        val hoursUntilClass = java.time.Duration.between(
            Instant.now(), booking.fitnessClass.startTime
        ).toHours()
        if (hoursUntilClass < cancellationWindowHours) {
            throw CancellationWindowClosedException(classId, cancellationWindowHours)
        }

        booking.status = BookingStatus.CANCELLED
        booking.cancelledAt = Instant.now()
        bookingRepository.save(booking)

        eventPublisher.publishEvent(
            BookingCancelledEvent(
                userId = userId,
                classId = classId,
                cancelledAt = booking.cancelledAt!!,
            )
        )

        promoteFromWaitlist(booking.fitnessClass)
    }

    // --- Waitlist ---

    fun getUserWaitlistEntries(userId: UUID): List<WaitlistEntryResponse> {
        return waitlistEntryRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .map { it.toResponse() }
    }

    @Transactional
    fun removeFromWaitlist(classId: UUID, userId: UUID) {
        val entry = waitlistEntryRepository.findByFitnessClassIdAndUserId(classId, userId)
            ?: throw WaitlistEntryNotFoundException(classId, userId)
        waitlistEntryRepository.delete(entry)

        val remaining = waitlistEntryRepository
            .findByFitnessClassIdOrderByPositionAsc(classId)
        remaining.forEachIndexed { index, e ->
            e.position = index + 1
            waitlistEntryRepository.save(e)
        }
    }

    // --- Private helpers ---

    private fun promoteFromWaitlist(fitnessClass: FitnessClass) {
        val waitlist = waitlistEntryRepository
            .findByFitnessClassIdOrderByPositionAsc(fitnessClass.id!!)
        if (waitlist.isEmpty()) return

        for (entry in waitlist) {
            val overlapping = bookingRepository
                .findOverlappingBookings(entry.userId, fitnessClass.startTime, fitnessClass.endTime)
            if (overlapping.isNotEmpty()) continue

            val booking = Booking(
                userId = entry.userId,
                fitnessClass = fitnessClass,
            )
            bookingRepository.save(booking)
            waitlistEntryRepository.delete(entry)

            val remaining = waitlistEntryRepository
                .findByFitnessClassIdOrderByPositionAsc(fitnessClass.id!!)
            remaining.forEachIndexed { index, e ->
                e.position = index + 1
                waitlistEntryRepository.save(e)
            }

            eventPublisher.publishEvent(
                WaitlistPromotedEvent(
                    userId = entry.userId,
                    classId = fitnessClass.id!!,
                    className = fitnessClass.name,
                    startTime = fitnessClass.startTime,
                )
            )
            return
        }
    }

    private fun FitnessClass.toSummary() = ClassSummary(
        id = id!!,
        name = name,
        description = description,
        classType = classType,
        startTime = startTime,
        endTime = endTime,
        locationId = locationId,
        coachId = coachId,
    )

    private fun FitnessClass.toDetail() = ClassDetail(
        id = id!!,
        name = name,
        description = description,
        classType = classType,
        coachId = coachId,
        room = room,
        startTime = startTime,
        endTime = endTime,
        capacity = capacity,
        locationId = locationId,
        status = status.name,
        enrolledCount = bookings.count { it.status == BookingStatus.CONFIRMED },
        waitlistSize = waitlistEntries.size,
        createdAt = createdAt!!,
    )
}

// --- Sealed result type for bookClass ---

internal sealed class BookClassResult {
    data class Booked(val booking: BookingResponse) : BookClassResult()
    data class Waitlisted(val waitlistEntry: WaitlistEntryResponse) : BookClassResult()
}

// --- Exception classes ---

internal class FitnessClassNotFoundException(id: UUID) :
    RuntimeException("Fitness class not found: $id")

internal class BookingNotFoundException(classId: UUID, userId: UUID) :
    RuntimeException("Booking not found for class $classId and user $userId")

internal class ScheduleConflictException(userId: UUID, startTime: java.time.Instant, endTime: java.time.Instant) :
    RuntimeException("User $userId has an overlapping booking between $startTime and $endTime")

internal class AlreadyBookedException(classId: UUID, userId: UUID) :
    RuntimeException("User $userId is already booked for class $classId")

internal class ClassNotBookableException(classId: UUID, reason: String) :
    RuntimeException("Class $classId is not bookable: $reason")

internal class CancellationWindowClosedException(classId: UUID, windowHours: Long) :
    RuntimeException("Cancellation window of ${windowHours}h has closed for class $classId")

internal class WaitlistFullException(classId: UUID) :
    RuntimeException("Waitlist is full for class $classId")

internal class DailyBookingLimitExceededException(userId: UUID, limit: Int) :
    RuntimeException("User $userId has reached the daily booking limit of $limit")

internal class WaitlistEntryNotFoundException(classId: UUID, userId: UUID) :
    RuntimeException("Waitlist entry not found for class $classId and user $userId")
