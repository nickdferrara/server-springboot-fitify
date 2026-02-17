package com.nickdferrara.fitify.scheduling.internal

import com.nickdferrara.fitify.scheduling.ClassCancelledEvent
import com.nickdferrara.fitify.scheduling.internal.entities.Booking
import com.nickdferrara.fitify.scheduling.internal.enums.BookingStatus
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import com.nickdferrara.fitify.scheduling.internal.enums.FitnessClassStatus
import com.nickdferrara.fitify.scheduling.internal.entities.WaitlistEntry
import com.nickdferrara.fitify.scheduling.internal.repository.BookingRepository
import com.nickdferrara.fitify.scheduling.internal.repository.FitnessClassRepository
import com.nickdferrara.fitify.scheduling.internal.repository.WaitlistEntryRepository
import com.nickdferrara.fitify.scheduling.internal.service.SchedulingQueryServiceImpl
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class SchedulingQueryServiceTest {

    private val fitnessClassRepository = mockk<FitnessClassRepository>()
    private val bookingRepository = mockk<BookingRepository>()
    private val waitlistEntryRepository = mockk<WaitlistEntryRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val service = SchedulingQueryServiceImpl(
        fitnessClassRepository, bookingRepository, waitlistEntryRepository, eventPublisher,
    )

    private val locationId: UUID = UUID.randomUUID()
    private val coachId: UUID = UUID.randomUUID()
    private val userId: UUID = UUID.randomUUID()

    private fun buildFitnessClass(
        id: UUID = UUID.randomUUID(),
        name: String = "Morning Yoga",
        classType: String = "yoga",
        capacity: Int = 20,
        status: FitnessClassStatus = FitnessClassStatus.ACTIVE,
        startTime: Instant = Instant.now().plus(2, ChronoUnit.DAYS),
        endTime: Instant = Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
    ) = FitnessClass(
        id = id,
        locationId = locationId,
        name = name,
        classType = classType,
        coachId = coachId,
        room = "Studio A",
        startTime = startTime,
        endTime = endTime,
        capacity = capacity,
        status = status,
        createdAt = Instant.now(),
    )

    private fun buildBooking(
        id: UUID = UUID.randomUUID(),
        fitnessClass: FitnessClass,
        userId: UUID = this.userId,
        status: BookingStatus = BookingStatus.CONFIRMED,
    ) = Booking(
        id = id,
        userId = userId,
        fitnessClass = fitnessClass,
        status = status,
        bookedAt = Instant.now(),
    )

    // --- Public API Tests ---

    @Test
    fun `findClassById returns summary when class exists`() {
        val id = UUID.randomUUID()
        val fc = buildFitnessClass(id = id)
        every { fitnessClassRepository.findById(id) } returns Optional.of(fc)

        val result = service.findClassById(id)

        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).value
        assertEquals(id, summary.id)
        assertEquals("Morning Yoga", summary.name)
    }

    @Test
    fun `findClassById returns failure when class does not exist`() {
        val id = UUID.randomUUID()
        every { fitnessClassRepository.findById(id) } returns Optional.empty()

        val result = service.findClassById(id)

        assertTrue(result is Result.Failure)
        val error = (result as Result.Failure).error
        assertTrue(error is NotFoundError)
    }

    @Test
    fun `findUpcomingClassesByLocationId delegates to repository`() {
        val fc = buildFitnessClass()
        every {
            fitnessClassRepository.findByLocationIdAndStartTimeAfterOrderByStartTimeAsc(locationId, any())
        } returns listOf(fc)

        val results = service.findUpcomingClassesByLocationId(locationId)

        assertEquals(1, results.size)
        assertEquals("Morning Yoga", results[0].name)
    }

    @Test
    fun `getClassDetail returns detail when class exists`() {
        val id = UUID.randomUUID()
        val fc = buildFitnessClass(id = id)
        every { fitnessClassRepository.findById(id) } returns Optional.of(fc)

        val result = service.getClassDetail(id)

        assertTrue(result is Result.Success)
        val detail = (result as Result.Success).value
        assertEquals(id, detail.id)
        assertEquals("Morning Yoga", detail.name)
    }

    @Test
    fun `getClassDetail returns failure when class does not exist`() {
        val id = UUID.randomUUID()
        every { fitnessClassRepository.findById(id) } returns Optional.empty()

        val result = service.getClassDetail(id)

        assertTrue(result is Result.Failure)
        val error = (result as Result.Failure).error
        assertTrue(error is NotFoundError)
    }

    @Test
    fun `findClassesByLocationId returns detail list`() {
        val fc = buildFitnessClass()
        every {
            fitnessClassRepository.findByLocationIdAndStartTimeAfterOrderByStartTimeAsc(locationId, any())
        } returns listOf(fc)

        val results = service.findClassesByLocationId(locationId)

        assertEquals(1, results.size)
        assertEquals("Morning Yoga", results[0].name)
    }

    @Test
    fun `findClassesByCoachIdAndTimeRange delegates to repository`() {
        val fc = buildFitnessClass()
        val start = Instant.now()
        val end = Instant.now().plus(7, ChronoUnit.DAYS)
        every { fitnessClassRepository.findByCoachIdAndTimeRange(coachId, start, end) } returns listOf(fc)

        val results = service.findClassesByCoachIdAndTimeRange(coachId, start, end)

        assertEquals(1, results.size)
        assertEquals("Morning Yoga", results[0].name)
    }

    @Test
    fun `cancelClass sets status cancels bookings and publishes event`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)
        val booking = buildBooking(fitnessClass = fc)

        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)
        every { bookingRepository.findByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED) } returns listOf(booking)
        every { bookingRepository.save(any()) } answers { firstArg() }
        every { waitlistEntryRepository.findByFitnessClassIdOrderByPositionAsc(classId) } returns emptyList()
        every { waitlistEntryRepository.deleteAll(any<List<WaitlistEntry>>()) } returns Unit
        every { fitnessClassRepository.save(any()) } answers { firstArg() }

        val result = service.cancelClass(classId)

        assertEquals(FitnessClassStatus.CANCELLED, fc.status)
        assertEquals(BookingStatus.CANCELLED, booking.status)
        assertEquals(classId, result.classId)
        assertEquals(1, result.affectedUserIds.size)

        val eventSlot = slot<ClassCancelledEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(classId, eventSlot.captured.classId)
    }

    @Test
    fun `countBookingCancellationsBetween delegates to repository`() {
        val start = Instant.now().minus(7, ChronoUnit.DAYS)
        val end = Instant.now()
        every { bookingRepository.countCancellationsBetween(start, end) } returns 5L

        val result = service.countBookingCancellationsBetween(start, end, null)

        assertEquals(5L, result)
    }

    @Test
    fun `countBookingCancellationsBetween with locationId delegates to repository`() {
        val start = Instant.now().minus(7, ChronoUnit.DAYS)
        val end = Instant.now()
        every { bookingRepository.countCancellationsBetweenAndLocationId(start, end, locationId) } returns 3L

        val result = service.countBookingCancellationsBetween(start, end, locationId)

        assertEquals(3L, result)
    }
}
