package com.nickdferrara.fitify.admin.internal

import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import com.nickdferrara.fitify.admin.internal.dtos.request.CreateClassRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.CreateRecurringScheduleRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.UpdateBusinessRuleRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.UpdateClassRequest
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
import com.nickdferrara.fitify.coaching.CoachSummary
import com.nickdferrara.fitify.coaching.CoachingApi
import com.nickdferrara.fitify.location.LocationApi
import com.nickdferrara.fitify.location.LocationSummary
import com.nickdferrara.fitify.scheduling.CancelClassResult
import com.nickdferrara.fitify.scheduling.ClassDetail
import com.nickdferrara.fitify.scheduling.ClassSummary
import com.nickdferrara.fitify.scheduling.CreateClassCommand
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.scheduling.UpdateClassCommand
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class AdminServiceTest {

    private val schedulingApi = mockk<SchedulingApi>()
    private val coachingApi = mockk<CoachingApi>()
    private val locationApi = mockk<LocationApi>()
    private val recurringScheduleRepository = mockk<RecurringScheduleRepository>()
    private val businessRuleRepository = mockk<BusinessRuleRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val service = AdminService(
        schedulingApi, coachingApi, locationApi, recurringScheduleRepository,
        businessRuleRepository, eventPublisher,
    )

    private val locationId: UUID = UUID.randomUUID()
    private val coachId: UUID = UUID.randomUUID()

    private fun buildLocationSummary(id: UUID = locationId) = LocationSummary(
        id = id,
        name = "Downtown Gym",
        address = "123 Main St",
        city = "New York",
        state = "NY",
        zipCode = "10001",
        timeZone = "America/New_York",
        active = true,
    )

    private fun buildCoachSummary(id: UUID = coachId, active: Boolean = true) = CoachSummary(
        id = id,
        name = "John Trainer",
        active = active,
    )

    private fun buildClassDetail(
        id: UUID = UUID.randomUUID(),
        name: String = "Morning Yoga",
        coachId: UUID = this.coachId,
        startTime: Instant = Instant.now().plus(2, ChronoUnit.DAYS),
        endTime: Instant = Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
    ) = ClassDetail(
        id = id,
        name = name,
        description = null,
        classType = "yoga",
        coachId = coachId,
        room = "Studio A",
        startTime = startTime,
        endTime = endTime,
        capacity = 20,
        locationId = locationId,
        status = "ACTIVE",
        enrolledCount = 5,
        waitlistSize = 0,
        createdAt = Instant.now(),
    )

    private fun buildClassSummary(
        id: UUID = UUID.randomUUID(),
        name: String = "Morning Yoga",
        coachId: UUID = this.coachId,
        startTime: Instant = Instant.now().plus(2, ChronoUnit.DAYS),
        endTime: Instant = Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
    ) = ClassSummary(
        id = id,
        name = name,
        description = null,
        classType = "yoga",
        startTime = startTime,
        endTime = endTime,
        locationId = locationId,
        coachId = coachId,
    )

    // --- createClass Tests ---

    @Test
    fun `createClass succeeds with valid inputs`() {
        val request = CreateClassRequest(
            name = "HIIT Blast",
            classType = "hiit",
            coachId = coachId,
            startTime = Instant.now().plus(1, ChronoUnit.DAYS),
            endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(45, ChronoUnit.MINUTES),
            capacity = 15,
        )

        every { locationApi.findLocationById(locationId) } returns Result.Success(buildLocationSummary())
        every { coachingApi.findCoachById(coachId) } returns Result.Success(buildCoachSummary())
        every { schedulingApi.findClassesByCoachIdAndTimeRange(coachId, any(), any()) } returns emptyList()
        val commandSlot = slot<CreateClassCommand>()
        every { schedulingApi.createClass(locationId, capture(commandSlot)) } answers {
            buildClassDetail(name = commandSlot.captured.name)
        }

        val response = service.createClass(locationId, request)

        assertEquals("HIIT Blast", response.name)
        verify { schedulingApi.createClass(locationId, any()) }
    }

    @Test
    fun `createClass throws when location not found`() {
        val request = CreateClassRequest(
            name = "HIIT",
            classType = "hiit",
            coachId = coachId,
            startTime = Instant.now().plus(1, ChronoUnit.DAYS),
            endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(45, ChronoUnit.MINUTES),
            capacity = 15,
        )

        every { locationApi.findLocationById(locationId) } returns Result.Failure(NotFoundError("not found"))

        assertThrows<LocationNotFoundException> {
            service.createClass(locationId, request)
        }
    }

    @Test
    fun `createClass throws when coach not found`() {
        val request = CreateClassRequest(
            name = "HIIT",
            classType = "hiit",
            coachId = coachId,
            startTime = Instant.now().plus(1, ChronoUnit.DAYS),
            endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(45, ChronoUnit.MINUTES),
            capacity = 15,
        )

        every { locationApi.findLocationById(locationId) } returns Result.Success(buildLocationSummary())
        every { coachingApi.findCoachById(coachId) } returns Result.Failure(NotFoundError("not found"))

        assertThrows<CoachNotFoundException> {
            service.createClass(locationId, request)
        }
    }

    @Test
    fun `createClass throws when coach is inactive`() {
        val request = CreateClassRequest(
            name = "HIIT",
            classType = "hiit",
            coachId = coachId,
            startTime = Instant.now().plus(1, ChronoUnit.DAYS),
            endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(45, ChronoUnit.MINUTES),
            capacity = 15,
        )

        every { locationApi.findLocationById(locationId) } returns Result.Success(buildLocationSummary())
        every { coachingApi.findCoachById(coachId) } returns Result.Success(buildCoachSummary(active = false))

        assertThrows<CoachNotFoundException> {
            service.createClass(locationId, request)
        }
    }

    @Test
    fun `createClass throws when coach has schedule conflict`() {
        val startTime = Instant.now().plus(1, ChronoUnit.DAYS)
        val endTime = startTime.plus(45, ChronoUnit.MINUTES)
        val request = CreateClassRequest(
            name = "HIIT",
            classType = "hiit",
            coachId = coachId,
            startTime = startTime,
            endTime = endTime,
            capacity = 15,
        )

        every { locationApi.findLocationById(locationId) } returns Result.Success(buildLocationSummary())
        every { coachingApi.findCoachById(coachId) } returns Result.Success(buildCoachSummary())
        every { schedulingApi.findClassesByCoachIdAndTimeRange(coachId, startTime, endTime) } returns
            listOf(buildClassSummary(name = "Existing Class"))

        assertThrows<CoachScheduleConflictException> {
            service.createClass(locationId, request)
        }
    }

    // --- updateClass Tests ---

    @Test
    fun `updateClass succeeds with valid inputs`() {
        val classId = UUID.randomUUID()
        val existing = buildClassDetail(id = classId)
        val request = UpdateClassRequest(name = "Updated Yoga")

        every { schedulingApi.getClassDetail(classId) } returns Result.Success(existing)
        val commandSlot = slot<UpdateClassCommand>()
        every { schedulingApi.updateClass(classId, capture(commandSlot)) } answers {
            existing.copy(name = "Updated Yoga")
        }

        val response = service.updateClass(classId, request)

        assertEquals("Updated Yoga", response.name)
    }

    @Test
    fun `updateClass throws when class not found`() {
        val classId = UUID.randomUUID()
        val request = UpdateClassRequest(name = "Updated")

        every { schedulingApi.getClassDetail(classId) } returns Result.Failure(NotFoundError("not found"))

        assertThrows<ClassNotFoundException> {
            service.updateClass(classId, request)
        }
    }

    @Test
    fun `updateClass validates coach conflict when time changes`() {
        val classId = UUID.randomUUID()
        val newStartTime = Instant.now().plus(3, ChronoUnit.DAYS)
        val newEndTime = newStartTime.plus(1, ChronoUnit.HOURS)
        val existing = buildClassDetail(id = classId)
        val request = UpdateClassRequest(startTime = newStartTime, endTime = newEndTime)

        every { schedulingApi.getClassDetail(classId) } returns Result.Success(existing)
        every { schedulingApi.findClassesByCoachIdAndTimeRange(coachId, newStartTime, newEndTime) } returns
            listOf(buildClassSummary(name = "Conflicting Class"))

        assertThrows<CoachScheduleConflictException> {
            service.updateClass(classId, request)
        }
    }

    @Test
    fun `updateClass excludes self in conflict check`() {
        val classId = UUID.randomUUID()
        val newStartTime = Instant.now().plus(3, ChronoUnit.DAYS)
        val newEndTime = newStartTime.plus(1, ChronoUnit.HOURS)
        val existing = buildClassDetail(id = classId)
        val request = UpdateClassRequest(startTime = newStartTime, endTime = newEndTime)

        every { schedulingApi.getClassDetail(classId) } returns Result.Success(existing)
        // Only the class itself conflicts — should be excluded
        every { schedulingApi.findClassesByCoachIdAndTimeRange(coachId, newStartTime, newEndTime) } returns
            listOf(buildClassSummary(id = classId))
        every { schedulingApi.updateClass(classId, any()) } returns existing.copy(startTime = newStartTime, endTime = newEndTime)

        val response = service.updateClass(classId, request)

        assertEquals(classId, response.id)
    }

    // --- cancelClass Tests ---

    @Test
    fun `cancelClass returns affected counts`() {
        val classId = UUID.randomUUID()
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val waitlistUserId = UUID.randomUUID()

        every { schedulingApi.cancelClass(classId) } returns CancelClassResult(
            classId = classId,
            className = "Morning Yoga",
            affectedUserIds = listOf(userId1, userId2),
            waitlistUserIds = listOf(waitlistUserId),
        )

        val response = service.cancelClass(classId)

        assertEquals(classId, response.classId)
        assertEquals("Morning Yoga", response.className)
        assertEquals(2, response.affectedBookings)
        assertEquals(1, response.affectedWaitlist)
    }

    // --- listClassesByLocation Tests ---

    @Test
    fun `listClassesByLocation succeeds`() {
        every { locationApi.findLocationById(locationId) } returns Result.Success(buildLocationSummary())
        every { schedulingApi.findClassesByLocationId(locationId) } returns listOf(buildClassDetail())

        val results = service.listClassesByLocation(locationId)

        assertEquals(1, results.size)
        assertEquals("Morning Yoga", results[0].name)
    }

    @Test
    fun `listClassesByLocation throws when location not found`() {
        every { locationApi.findLocationById(locationId) } returns Result.Failure(NotFoundError("not found"))

        assertThrows<LocationNotFoundException> {
            service.listClassesByLocation(locationId)
        }
    }

    // --- createRecurringSchedule Tests ---

    @Test
    fun `createRecurringSchedule creates classes for matching days`() {
        val startDate = LocalDate.of(2025, 6, 2) // Monday
        val endDate = LocalDate.of(2025, 6, 8)   // Sunday — 1 week
        val request = CreateRecurringScheduleRequest(
            name = "Weekly Yoga",
            classType = "yoga",
            coachId = coachId,
            daysOfWeek = listOf("MONDAY", "WEDNESDAY", "FRIDAY"),
            startTime = LocalTime.of(9, 0),
            durationMinutes = 60,
            capacity = 20,
            startDate = startDate,
            endDate = endDate,
        )

        every { locationApi.findLocationById(locationId) } returns Result.Success(buildLocationSummary())
        every { coachingApi.findCoachById(coachId) } returns Result.Success(buildCoachSummary())
        every { schedulingApi.findClassesByCoachIdAndTimeRange(coachId, any(), any()) } returns emptyList()
        every { schedulingApi.createClass(locationId, any()) } answers { buildClassDetail() }
        every { recurringScheduleRepository.save(any()) } answers {
            val s = firstArg<RecurringSchedule>()
            RecurringSchedule(
                id = UUID.randomUUID(),
                locationId = s.locationId,
                name = s.name,
                description = s.description,
                classType = s.classType,
                coachId = s.coachId,
                room = s.room,
                daysOfWeek = s.daysOfWeek,
                startTime = s.startTime,
                durationMinutes = s.durationMinutes,
                capacity = s.capacity,
                startDate = s.startDate,
                endDate = s.endDate,
                createdAt = Instant.now(),
            )
        }

        val response = service.createRecurringSchedule(locationId, request)

        assertEquals(3, response.classesCreated) // Mon, Wed, Fri
        assertEquals("Weekly Yoga", response.name)
        verify(exactly = 3) { schedulingApi.createClass(locationId, any()) }
    }

    @Test
    fun `createRecurringSchedule throws when end date before start date`() {
        val request = CreateRecurringScheduleRequest(
            name = "Yoga",
            classType = "yoga",
            coachId = coachId,
            daysOfWeek = listOf("MONDAY"),
            startTime = LocalTime.of(9, 0),
            durationMinutes = 60,
            capacity = 20,
            startDate = LocalDate.of(2025, 6, 10),
            endDate = LocalDate.of(2025, 6, 1),
        )

        assertThrows<InvalidRecurringScheduleException> {
            service.createRecurringSchedule(locationId, request)
        }
    }

    @Test
    fun `createRecurringSchedule throws when days of week is empty`() {
        val request = CreateRecurringScheduleRequest(
            name = "Yoga",
            classType = "yoga",
            coachId = coachId,
            daysOfWeek = emptyList(),
            startTime = LocalTime.of(9, 0),
            durationMinutes = 60,
            capacity = 20,
            startDate = LocalDate.of(2025, 6, 2),
            endDate = LocalDate.of(2025, 6, 8),
        )

        assertThrows<InvalidRecurringScheduleException> {
            service.createRecurringSchedule(locationId, request)
        }
    }

    @Test
    fun `createRecurringSchedule skips conflicting slots`() {
        val startDate = LocalDate.of(2025, 6, 2) // Monday
        val endDate = LocalDate.of(2025, 6, 4)   // Wednesday — 2 target days (Mon, Wed)
        val request = CreateRecurringScheduleRequest(
            name = "Yoga",
            classType = "yoga",
            coachId = coachId,
            daysOfWeek = listOf("MONDAY", "WEDNESDAY"),
            startTime = LocalTime.of(9, 0),
            durationMinutes = 60,
            capacity = 20,
            startDate = startDate,
            endDate = endDate,
        )

        every { locationApi.findLocationById(locationId) } returns Result.Success(buildLocationSummary())
        every { coachingApi.findCoachById(coachId) } returns Result.Success(buildCoachSummary())
        // First call (Monday) — conflict exists; second call (Wednesday) — no conflict
        every { schedulingApi.findClassesByCoachIdAndTimeRange(coachId, any(), any()) } returns
            listOf(buildClassSummary()) andThen emptyList()
        every { schedulingApi.createClass(locationId, any()) } answers { buildClassDetail() }
        every { recurringScheduleRepository.save(any()) } answers {
            val s = firstArg<RecurringSchedule>()
            RecurringSchedule(
                id = UUID.randomUUID(),
                locationId = s.locationId,
                name = s.name,
                description = s.description,
                classType = s.classType,
                coachId = s.coachId,
                room = s.room,
                daysOfWeek = s.daysOfWeek,
                startTime = s.startTime,
                durationMinutes = s.durationMinutes,
                capacity = s.capacity,
                startDate = s.startDate,
                endDate = s.endDate,
                createdAt = Instant.now(),
            )
        }

        val response = service.createRecurringSchedule(locationId, request)

        assertEquals(1, response.classesCreated) // Only Wednesday created
        verify(exactly = 1) { schedulingApi.createClass(locationId, any()) }
    }

    // --- getBusinessRuleValue Tests ---

    @Test
    fun `getBusinessRuleValue returns global value when no location specified`() {
        val rule = BusinessRule(
            id = UUID.randomUUID(), ruleKey = "max_waitlist_size", value = "20", updatedBy = "system",
        )
        every { businessRuleRepository.findByRuleKeyAndLocationIdIsNull("max_waitlist_size") } returns rule

        val result = service.getBusinessRuleValue("max_waitlist_size")

        assertEquals("20", result)
    }

    @Test
    fun `getBusinessRuleValue returns location override when available`() {
        val override = BusinessRule(
            id = UUID.randomUUID(), ruleKey = "max_waitlist_size", value = "10",
            locationId = locationId, updatedBy = "admin",
        )
        every { businessRuleRepository.findByRuleKeyAndLocationId("max_waitlist_size", locationId) } returns override

        val result = service.getBusinessRuleValue("max_waitlist_size", locationId)

        assertEquals("10", result)
    }

    @Test
    fun `getBusinessRuleValue falls back to global when no location override`() {
        val global = BusinessRule(
            id = UUID.randomUUID(), ruleKey = "max_waitlist_size", value = "20", updatedBy = "system",
        )
        every { businessRuleRepository.findByRuleKeyAndLocationId("max_waitlist_size", locationId) } returns null
        every { businessRuleRepository.findByRuleKeyAndLocationIdIsNull("max_waitlist_size") } returns global

        val result = service.getBusinessRuleValue("max_waitlist_size", locationId)

        assertEquals("20", result)
    }

    @Test
    fun `getBusinessRuleValue returns null when rule does not exist`() {
        every { businessRuleRepository.findByRuleKeyAndLocationIdIsNull("nonexistent") } returns null

        val result = service.getBusinessRuleValue("nonexistent")

        assertNull(result)
    }

    // --- listBusinessRules Tests ---

    @Test
    fun `listBusinessRules returns all rules sorted`() {
        val rules = listOf(
            BusinessRule(
                id = UUID.randomUUID(), ruleKey = "cancellation_window_hours", value = "24", updatedBy = "system",
            ),
            BusinessRule(
                id = UUID.randomUUID(), ruleKey = "max_waitlist_size", value = "20", updatedBy = "system",
            ),
        )
        every { businessRuleRepository.findAllByOrderByRuleKeyAscLocationIdAsc() } returns rules

        val result = service.listBusinessRules()

        assertEquals(2, result.size)
        assertEquals("cancellation_window_hours", result[0].ruleKey)
        assertEquals("max_waitlist_size", result[1].ruleKey)
    }

    // --- updateBusinessRule Tests ---

    @Test
    fun `updateBusinessRule updates existing global rule`() {
        val existing = BusinessRule(
            id = UUID.randomUUID(), ruleKey = "max_waitlist_size", value = "20", updatedBy = "system",
        )
        val request = UpdateBusinessRuleRequest(value = "30")

        every { businessRuleRepository.findByRuleKeyAndLocationIdIsNull("max_waitlist_size") } returns existing
        every { businessRuleRepository.save(any()) } answers { firstArg() }

        val result = service.updateBusinessRule("max_waitlist_size", request, "admin@test.com")

        assertEquals("30", result.value)
        assertEquals("admin@test.com", result.updatedBy)
        verify { eventPublisher.publishEvent(any<BusinessRuleUpdatedEvent>()) }
    }

    @Test
    fun `updateBusinessRule creates location override when global exists`() {
        val global = BusinessRule(
            id = UUID.randomUUID(), ruleKey = "max_waitlist_size", value = "20",
            description = "Max waitlist", updatedBy = "system",
        )
        val request = UpdateBusinessRuleRequest(value = "10", locationId = locationId)

        every { businessRuleRepository.findByRuleKeyAndLocationId("max_waitlist_size", locationId) } returns null
        every { businessRuleRepository.findByRuleKeyAndLocationIdIsNull("max_waitlist_size") } returns global
        every { businessRuleRepository.save(any()) } answers {
            val rule = firstArg<BusinessRule>()
            BusinessRule(
                id = UUID.randomUUID(), ruleKey = rule.ruleKey, value = rule.value,
                locationId = rule.locationId, description = rule.description, updatedBy = rule.updatedBy,
            )
        }

        val result = service.updateBusinessRule("max_waitlist_size", request, "admin@test.com")

        assertEquals("10", result.value)
        assertEquals(locationId, result.locationId)
        verify { eventPublisher.publishEvent(any<BusinessRuleUpdatedEvent>()) }
    }

    @Test
    fun `updateBusinessRule throws when global rule not found`() {
        val request = UpdateBusinessRuleRequest(value = "10", locationId = locationId)

        every { businessRuleRepository.findByRuleKeyAndLocationId("nonexistent", locationId) } returns null
        every { businessRuleRepository.findByRuleKeyAndLocationIdIsNull("nonexistent") } returns null

        assertThrows<BusinessRuleNotFoundException> {
            service.updateBusinessRule("nonexistent", request, "admin@test.com")
        }
    }
}
