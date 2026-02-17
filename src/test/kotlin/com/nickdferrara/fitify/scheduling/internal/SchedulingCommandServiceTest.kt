package com.nickdferrara.fitify.scheduling.internal

import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassBookedEvent
import com.nickdferrara.fitify.scheduling.ClassFullEvent
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.scheduling.internal.dtos.request.CreateClassRequest
import com.nickdferrara.fitify.scheduling.internal.dtos.request.UpdateClassRequest
import com.nickdferrara.fitify.scheduling.internal.entities.Booking
import com.nickdferrara.fitify.scheduling.internal.enums.BookingStatus
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import com.nickdferrara.fitify.scheduling.internal.enums.FitnessClassStatus
import com.nickdferrara.fitify.scheduling.internal.entities.WaitlistEntry
import com.nickdferrara.fitify.scheduling.internal.repository.BookingRepository
import com.nickdferrara.fitify.scheduling.internal.repository.FitnessClassRepository
import com.nickdferrara.fitify.scheduling.internal.repository.WaitlistEntryRepository
import com.nickdferrara.fitify.scheduling.internal.exceptions.AlreadyBookedException
import com.nickdferrara.fitify.scheduling.internal.exceptions.BookingNotFoundException
import com.nickdferrara.fitify.scheduling.internal.exceptions.CancellationWindowClosedException
import com.nickdferrara.fitify.scheduling.internal.exceptions.ClassNotBookableException
import com.nickdferrara.fitify.scheduling.internal.exceptions.DailyBookingLimitExceededException
import com.nickdferrara.fitify.scheduling.internal.exceptions.FitnessClassNotFoundException
import com.nickdferrara.fitify.scheduling.internal.exceptions.ScheduleConflictException
import com.nickdferrara.fitify.scheduling.internal.exceptions.WaitlistEntryNotFoundException
import com.nickdferrara.fitify.scheduling.internal.exceptions.WaitlistFullException
import com.nickdferrara.fitify.scheduling.internal.model.BookClassResult
import com.nickdferrara.fitify.scheduling.internal.service.SchedulingCommandServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class SchedulingCommandServiceTest {

    private val fitnessClassRepository = mockk<FitnessClassRepository>()
    private val bookingRepository = mockk<BookingRepository>()
    private val waitlistEntryRepository = mockk<WaitlistEntryRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val service = SchedulingCommandServiceImpl(
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

    // --- Internal CRUD Tests ---

    @Test
    fun `createClass persists and returns response`() {
        val request = CreateClassRequest(
            name = "HIIT Blast",
            classType = "hiit",
            coachId = coachId,
            room = "Studio B",
            startTime = Instant.now().plus(1, ChronoUnit.DAYS),
            endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(45, ChronoUnit.MINUTES),
            capacity = 15,
        )

        val savedId = UUID.randomUUID()
        every { fitnessClassRepository.save(any()) } answers {
            val fc = firstArg<FitnessClass>()
            FitnessClass(
                id = savedId,
                locationId = fc.locationId,
                name = fc.name,
                classType = fc.classType,
                coachId = fc.coachId,
                room = fc.room,
                startTime = fc.startTime,
                endTime = fc.endTime,
                capacity = fc.capacity,
                createdAt = Instant.now(),
            )
        }

        val response = service.createClass(locationId, request)

        assertEquals(savedId, response.id)
        assertEquals("HIIT Blast", response.name)
        assertEquals("hiit", response.classType)
        assertEquals(15, response.capacity)
    }

    @Test
    fun `getClass throws when class not found`() {
        val id = UUID.randomUUID()
        every { fitnessClassRepository.findById(id) } returns Optional.empty()

        assertThrows<FitnessClassNotFoundException> {
            service.getClass(id)
        }
    }

    @Test
    fun `updateClass updates fields and returns response`() {
        val id = UUID.randomUUID()
        val existing = buildFitnessClass(id = id, name = "Old Name", capacity = 10)
        every { fitnessClassRepository.findById(id) } returns Optional.of(existing)
        every { fitnessClassRepository.save(any()) } answers { firstArg() }

        val request = UpdateClassRequest(name = "New Name", capacity = 25)

        val response = service.updateClass(id, request)

        assertEquals("New Name", response.name)
        assertEquals(25, response.capacity)
        assertEquals("Studio A", response.room)
    }

    @Test
    fun `cancelClassInternal sets status and cancels all bookings`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)
        val booking = buildBooking(fitnessClass = fc)

        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)
        every { bookingRepository.findByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED) } returns listOf(booking)
        every { bookingRepository.save(any()) } answers { firstArg() }
        every { waitlistEntryRepository.findByFitnessClassIdOrderByPositionAsc(classId) } returns emptyList()
        every { waitlistEntryRepository.deleteAll(any<List<WaitlistEntry>>()) } returns Unit
        every { fitnessClassRepository.save(any()) } answers { firstArg() }

        service.cancelClassInternal(classId)

        assertEquals(FitnessClassStatus.CANCELLED, fc.status)
        assertEquals(BookingStatus.CANCELLED, booking.status)
    }

    // --- Booking Tests ---

    @Test
    fun `bookClass creates booking when capacity available`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId, capacity = 20)

        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)
        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns null
        every { bookingRepository.findOverlappingBookings(userId, any(), any()) } returns emptyList()
        every { bookingRepository.countUserBookingsForDay(userId, any(), any()) } returns 0
        every { bookingRepository.countByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED) } returns 5
        every { bookingRepository.save(any()) } answers {
            val b = firstArg<Booking>()
            Booking(
                id = UUID.randomUUID(),
                userId = b.userId,
                fitnessClass = b.fitnessClass,
                status = b.status,
                bookedAt = Instant.now(),
            )
        }

        val result = service.bookClass(classId, userId)

        assertTrue(result is BookClassResult.Booked)
        val eventSlot = slot<ClassBookedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(classId, eventSlot.captured.classId)
        assertEquals(userId, eventSlot.captured.userId)
    }

    @Test
    fun `bookClass adds to waitlist when class is full`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId, capacity = 1)

        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)
        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns null
        every { bookingRepository.findOverlappingBookings(userId, any(), any()) } returns emptyList()
        every { bookingRepository.countUserBookingsForDay(userId, any(), any()) } returns 0
        every { bookingRepository.countByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED) } returns 1
        every { waitlistEntryRepository.countByFitnessClassId(classId) } returns 0
        every { waitlistEntryRepository.findByFitnessClassIdAndUserId(classId, userId) } returns null
        every { waitlistEntryRepository.save(any()) } answers {
            val e = firstArg<WaitlistEntry>()
            WaitlistEntry(
                id = UUID.randomUUID(),
                userId = e.userId,
                fitnessClass = e.fitnessClass,
                position = e.position,
                createdAt = Instant.now(),
            )
        }

        val result = service.bookClass(classId, userId)

        assertTrue(result is BookClassResult.Waitlisted)
        val eventSlot = slot<ClassFullEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(classId, eventSlot.captured.classId)
    }

    @Test
    fun `bookClass throws when class is cancelled`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId, status = FitnessClassStatus.CANCELLED)
        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)

        assertThrows<ClassNotBookableException> {
            service.bookClass(classId, userId)
        }
    }

    @Test
    fun `bookClass throws when class has already started`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(
            id = classId,
            startTime = Instant.now().minus(1, ChronoUnit.HOURS),
            endTime = Instant.now(),
        )
        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)

        assertThrows<ClassNotBookableException> {
            service.bookClass(classId, userId)
        }
    }

    @Test
    fun `bookClass throws when user already booked`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)
        val existing = buildBooking(fitnessClass = fc)

        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)
        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns existing

        assertThrows<AlreadyBookedException> {
            service.bookClass(classId, userId)
        }
    }

    @Test
    fun `bookClass throws when overlapping booking exists`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)
        val otherFc = buildFitnessClass()
        val overlap = buildBooking(fitnessClass = otherFc)

        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)
        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns null
        every { bookingRepository.findOverlappingBookings(userId, any(), any()) } returns listOf(overlap)

        assertThrows<ScheduleConflictException> {
            service.bookClass(classId, userId)
        }
    }

    @Test
    fun `bookClass throws when daily booking limit exceeded`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)

        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)
        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns null
        every { bookingRepository.findOverlappingBookings(userId, any(), any()) } returns emptyList()
        every { bookingRepository.countUserBookingsForDay(userId, any(), any()) } returns 3

        assertThrows<DailyBookingLimitExceededException> {
            service.bookClass(classId, userId)
        }
    }

    @Test
    fun `bookClass throws when class not found`() {
        val classId = UUID.randomUUID()
        every { fitnessClassRepository.findById(classId) } returns Optional.empty()

        assertThrows<FitnessClassNotFoundException> {
            service.bookClass(classId, userId)
        }
    }

    @Test
    fun `bookClass throws when waitlist is full`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId, capacity = 1)

        every { fitnessClassRepository.findById(classId) } returns Optional.of(fc)
        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns null
        every { bookingRepository.findOverlappingBookings(userId, any(), any()) } returns emptyList()
        every { bookingRepository.countUserBookingsForDay(userId, any(), any()) } returns 0
        every { bookingRepository.countByFitnessClassIdAndStatus(classId, BookingStatus.CONFIRMED) } returns 1
        every { waitlistEntryRepository.countByFitnessClassId(classId) } returns 20

        assertThrows<WaitlistFullException> {
            service.bookClass(classId, userId)
        }
    }

    // --- Cancellation Tests ---

    @Test
    fun `cancelBooking soft-deletes booking and publishes event`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)
        val booking = buildBooking(fitnessClass = fc)

        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns booking
        every { bookingRepository.save(any()) } answers { firstArg() }
        every { waitlistEntryRepository.findByFitnessClassIdOrderByPositionAsc(classId) } returns emptyList()

        service.cancelBooking(classId, userId)

        assertEquals(BookingStatus.CANCELLED, booking.status)
        assertTrue(booking.cancelledAt != null)

        val eventSlot = slot<BookingCancelledEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(classId, eventSlot.captured.classId)
        assertEquals(userId, eventSlot.captured.userId)
    }

    @Test
    fun `cancelBooking throws when cancellation window closed`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(
            id = classId,
            startTime = Instant.now().plus(1, ChronoUnit.HOURS),
            endTime = Instant.now().plus(2, ChronoUnit.HOURS),
        )
        val booking = buildBooking(fitnessClass = fc)

        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns booking

        assertThrows<CancellationWindowClosedException> {
            service.cancelBooking(classId, userId)
        }
    }

    @Test
    fun `cancelBooking throws when booking not found`() {
        val classId = UUID.randomUUID()

        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns null

        assertThrows<BookingNotFoundException> {
            service.cancelBooking(classId, userId)
        }
    }

    @Test
    fun `cancelBooking promotes first eligible waitlist entry`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)
        val booking = buildBooking(fitnessClass = fc)
        val waitlistUserId = UUID.randomUUID()
        val waitlistEntry = WaitlistEntry(
            id = UUID.randomUUID(),
            userId = waitlistUserId,
            fitnessClass = fc,
            position = 1,
            createdAt = Instant.now(),
        )

        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns booking
        every { bookingRepository.save(any()) } answers { firstArg() }
        every { waitlistEntryRepository.findByFitnessClassIdOrderByPositionAsc(classId) } returns listOf(waitlistEntry) andThen emptyList()
        every { bookingRepository.findOverlappingBookings(waitlistUserId, any(), any()) } returns emptyList()
        every { waitlistEntryRepository.delete(waitlistEntry) } returns Unit

        service.cancelBooking(classId, userId)

        verify { bookingRepository.save(match<Booking> { it.userId == waitlistUserId }) }

        val eventSlot = slot<WaitlistPromotedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(waitlistUserId, eventSlot.captured.userId)
        assertEquals(classId, eventSlot.captured.classId)
    }

    @Test
    fun `cancelBooking skips waitlist entry with overlapping booking`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)
        val booking = buildBooking(fitnessClass = fc)
        val conflictUserId = UUID.randomUUID()
        val eligibleUserId = UUID.randomUUID()
        val conflictEntry = WaitlistEntry(
            id = UUID.randomUUID(),
            userId = conflictUserId,
            fitnessClass = fc,
            position = 1,
            createdAt = Instant.now(),
        )
        val eligibleEntry = WaitlistEntry(
            id = UUID.randomUUID(),
            userId = eligibleUserId,
            fitnessClass = fc,
            position = 2,
            createdAt = Instant.now(),
        )

        every { bookingRepository.findByFitnessClassIdAndUserIdAndStatus(classId, userId, BookingStatus.CONFIRMED) } returns booking
        every { bookingRepository.save(any()) } answers { firstArg() }
        every { waitlistEntryRepository.findByFitnessClassIdOrderByPositionAsc(classId) } returns
            listOf(conflictEntry, eligibleEntry) andThen emptyList()
        every { bookingRepository.findOverlappingBookings(conflictUserId, any(), any()) } returns listOf(buildBooking(fitnessClass = buildFitnessClass(), userId = conflictUserId))
        every { bookingRepository.findOverlappingBookings(eligibleUserId, any(), any()) } returns emptyList()
        every { waitlistEntryRepository.delete(eligibleEntry) } returns Unit

        service.cancelBooking(classId, userId)

        verify { bookingRepository.save(match<Booking> { it.userId == eligibleUserId }) }
        verify(exactly = 0) { bookingRepository.save(match<Booking> { it.userId == conflictUserId }) }
    }

    // --- Waitlist Tests ---

    @Test
    fun `getUserWaitlistEntries returns user entries`() {
        val fc = buildFitnessClass()
        val entry = WaitlistEntry(
            id = UUID.randomUUID(),
            userId = userId,
            fitnessClass = fc,
            position = 1,
            createdAt = Instant.now(),
        )
        every { waitlistEntryRepository.findByUserIdOrderByCreatedAtDesc(userId) } returns listOf(entry)

        val results = service.getUserWaitlistEntries(userId)

        assertEquals(1, results.size)
        assertEquals(userId, results[0].userId)
    }

    @Test
    fun `removeFromWaitlist deletes entry and reorders positions`() {
        val classId = UUID.randomUUID()
        val fc = buildFitnessClass(id = classId)
        val entry = WaitlistEntry(
            id = UUID.randomUUID(),
            userId = userId,
            fitnessClass = fc,
            position = 1,
            createdAt = Instant.now(),
        )
        val otherEntry = WaitlistEntry(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            fitnessClass = fc,
            position = 2,
            createdAt = Instant.now(),
        )

        every { waitlistEntryRepository.findByFitnessClassIdAndUserId(classId, userId) } returns entry
        every { waitlistEntryRepository.delete(entry) } returns Unit
        every { waitlistEntryRepository.findByFitnessClassIdOrderByPositionAsc(classId) } returns listOf(otherEntry)
        every { waitlistEntryRepository.save(any()) } answers { firstArg() }

        service.removeFromWaitlist(classId, userId)

        verify { waitlistEntryRepository.delete(entry) }
        assertEquals(1, otherEntry.position)
    }

    @Test
    fun `removeFromWaitlist throws when entry not found`() {
        val classId = UUID.randomUUID()

        every { waitlistEntryRepository.findByFitnessClassIdAndUserId(classId, userId) } returns null

        assertThrows<WaitlistEntryNotFoundException> {
            service.removeFromWaitlist(classId, userId)
        }
    }
}
