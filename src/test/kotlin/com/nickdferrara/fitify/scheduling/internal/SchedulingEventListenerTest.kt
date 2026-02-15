package com.nickdferrara.fitify.scheduling.internal

import com.nickdferrara.fitify.coaching.CoachAssignedEvent
import com.nickdferrara.fitify.coaching.CoachUpdatedEvent
import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import com.nickdferrara.fitify.scheduling.internal.service.SchedulingEventListener
import com.nickdferrara.fitify.scheduling.internal.service.SchedulingService
import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SchedulingEventListenerTest {

    private val schedulingService = mockk<SchedulingService>(relaxed = true)
    private val listener = SchedulingEventListener(schedulingService)

    @Test
    fun `onBusinessRuleUpdated updates cancellation window hours`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "cancellation_window_hours",
            newValue = "48",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)

        verify { schedulingService.cancellationWindowHours = 48L }
    }

    @Test
    fun `onBusinessRuleUpdated updates max waitlist size`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "max_waitlist_size",
            newValue = "30",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)

        verify { schedulingService.maxWaitlistSize = 30 }
    }

    @Test
    fun `onBusinessRuleUpdated updates max bookings per day`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "max_bookings_per_user_per_day",
            newValue = "5",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)

        verify { schedulingService.maxBookingsPerDay = 5 }
    }

    @Test
    fun `onCoachAssigned handles event without error`() {
        val event = CoachAssignedEvent(coachId = UUID.randomUUID(), classId = UUID.randomUUID())

        listener.onCoachAssigned(event)
    }

    @Test
    fun `onCoachUpdated handles event without error`() {
        val event = CoachUpdatedEvent(coachId = UUID.randomUUID(), updatedFields = setOf("name", "bio"))

        listener.onCoachUpdated(event)
    }

    @Test
    fun `onSubscriptionCancelled handles event without error`() {
        val event = SubscriptionCancelledEvent(
            subscriptionId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            effectiveDate = Instant.now(),
        )

        listener.onSubscriptionCancelled(event)
    }

    @Test
    fun `onSubscriptionExpired handles event without error`() {
        val event = SubscriptionExpiredEvent(
            subscriptionId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
        )

        listener.onSubscriptionExpired(event)
    }

    @Test
    fun `onLocationCreated handles event without error`() {
        val event = LocationCreatedEvent(
            locationId = UUID.randomUUID(),
            name = "Downtown Gym",
            address = "123 Main St",
            timeZone = "America/New_York",
        )

        listener.onLocationCreated(event)
    }

    @Test
    fun `onLocationUpdated handles event without error`() {
        val event = LocationUpdatedEvent(
            locationId = UUID.randomUUID(),
            updatedFields = setOf("name", "address"),
        )

        listener.onLocationUpdated(event)
    }

    @Test
    fun `onLocationDeactivated handles event without error`() {
        val event = LocationDeactivatedEvent(
            locationId = UUID.randomUUID(),
            effectiveDate = Instant.now(),
        )

        listener.onLocationDeactivated(event)
    }
}
