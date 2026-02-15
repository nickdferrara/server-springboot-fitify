package com.nickdferrara.fitify

import com.nickdferrara.fitify.coaching.CoachAssignedEvent
import com.nickdferrara.fitify.coaching.CoachCreatedEvent
import com.nickdferrara.fitify.coaching.CoachDeactivatedEvent
import com.nickdferrara.fitify.coaching.CoachUpdatedEvent
import com.nickdferrara.fitify.identity.PasswordResetRequestedEvent
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import com.nickdferrara.fitify.notification.NotificationFailedEvent
import com.nickdferrara.fitify.notification.NotificationSentEvent
import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassBookedEvent
import com.nickdferrara.fitify.scheduling.ClassCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassFullEvent
import com.nickdferrara.fitify.scheduling.ClassUpdatedEvent
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import com.nickdferrara.fitify.subscription.SubscriptionRenewedEvent
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EventCatalogTest {

    @Test
    fun `all 22 domain events are defined with correct payloads`() {
        val userId = UUID.randomUUID()
        val classId = UUID.randomUUID()
        val locationId = UUID.randomUUID()
        val coachId = UUID.randomUUID()
        val subscriptionId = UUID.randomUUID()
        val notificationId = UUID.randomUUID()
        val now = Instant.now()

        val events = listOf(
            UserRegisteredEvent(userId, "test@test.com", "John", "Doe"),
            PasswordResetRequestedEvent(userId, "test@test.com", "/reset-password?token=abc", now),
            ClassBookedEvent(userId, classId, "Yoga", now),
            BookingCancelledEvent(userId, classId, now),
            ClassCancelledEvent(classId, "Yoga", locationId, now, listOf(userId), emptyList(), now),
            ClassUpdatedEvent(classId, "Yoga", locationId, listOf("startTime"), listOf(userId)),
            ClassFullEvent(classId, "Yoga", 5),
            WaitlistPromotedEvent(userId, classId, "Yoga", now),
            CoachCreatedEvent(coachId, "Jane"),
            CoachUpdatedEvent(coachId, setOf("name")),
            CoachDeactivatedEvent(coachId, now),
            CoachAssignedEvent(coachId, classId),
            LocationCreatedEvent(locationId, "Downtown", "123 Main", "America/New_York"),
            LocationUpdatedEvent(locationId, setOf("name")),
            LocationDeactivatedEvent(locationId, now),
            SubscriptionCreatedEvent(subscriptionId, userId, "MONTHLY", "sub_123", now),
            SubscriptionRenewedEvent(subscriptionId, userId, now, "MONTHLY"),
            SubscriptionCancelledEvent(subscriptionId, userId, now),
            SubscriptionExpiredEvent(subscriptionId, userId),
            NotificationSentEvent(notificationId, "EMAIL", "DELIVERED"),
            NotificationFailedEvent(notificationId, "EMAIL", "Connection refused"),
            BusinessRuleUpdatedEvent("max_waitlist_size", "30", locationId, "admin"),
        )

        assertEquals(22, events.size, "Expected 22 domain events in the catalog")
        events.forEach { assertNotNull(it, "Event should not be null") }
    }

    @Test
    fun `SubscriptionCreatedEvent includes expiresAt field`() {
        val expiresAt = Instant.now().plusSeconds(86400)
        val event = SubscriptionCreatedEvent(
            subscriptionId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            planType = "ANNUAL",
            stripeSubscriptionId = "sub_456",
            expiresAt = expiresAt,
        )
        assertEquals(expiresAt, event.expiresAt)
    }

    @Test
    fun `SubscriptionRenewedEvent includes planType field`() {
        val event = SubscriptionRenewedEvent(
            subscriptionId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            newPeriodEnd = Instant.now(),
            planType = "ANNUAL",
        )
        assertEquals("ANNUAL", event.planType)
    }

    @Test
    fun `PasswordResetRequestedEvent uses resetLink and expiresAt`() {
        val expiresAt = Instant.now().plusSeconds(1800)
        val event = PasswordResetRequestedEvent(
            userId = UUID.randomUUID(),
            email = "test@test.com",
            resetLink = "/reset-password?token=abc123",
            expiresAt = expiresAt,
        )
        assertEquals("/reset-password?token=abc123", event.resetLink)
        assertEquals(expiresAt, event.expiresAt)
    }

    @Test
    fun `CoachAssignedEvent has correct payload`() {
        val coachId = UUID.randomUUID()
        val classId = UUID.randomUUID()
        val event = CoachAssignedEvent(coachId = coachId, classId = classId)
        assertEquals(coachId, event.coachId)
        assertEquals(classId, event.classId)
    }
}
