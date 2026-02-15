package com.nickdferrara.fitify.notification.internal

import com.nickdferrara.fitify.identity.PasswordResetRequestedEvent
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.notification.internal.service.NotificationPayloadFactory
import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassBookedEvent
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import com.nickdferrara.fitify.subscription.SubscriptionRenewedEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class NotificationPayloadFactoryTest {

    private val factory = NotificationPayloadFactory()
    private val userId: UUID = UUID.randomUUID()

    @Test
    fun `fromUserRegistered creates email-only payload`() {
        val event = UserRegisteredEvent(userId, "test@example.com", "John", "Doe")

        val payload = factory.fromUserRegistered(event)

        assertEquals("Welcome to Fitify!", payload.subject)
        assertEquals(setOf(NotificationChannel.EMAIL), payload.channels)
        assertEquals("test@example.com", payload.data["email"])
        assertTrue(payload.body.contains("John"))
    }

    @Test
    fun `fromPasswordResetRequested creates email-only payload`() {
        val event = PasswordResetRequestedEvent(userId, "test@example.com", "/reset-password?token=reset-token-123", Instant.now().plusSeconds(1800))

        val payload = factory.fromPasswordResetRequested(event)

        assertEquals("Reset your password", payload.subject)
        assertEquals(setOf(NotificationChannel.EMAIL), payload.channels)
        assertEquals("test@example.com", payload.data["email"])
        assertTrue(payload.body.contains("/reset-password?token=reset-token-123"))
    }

    @Test
    fun `fromClassBooked creates push and email payload`() {
        val classId = UUID.randomUUID()
        val event = ClassBookedEvent(userId, classId, "Morning Yoga", Instant.now())

        val payload = factory.fromClassBooked(event)

        assertEquals("Booking confirmed", payload.subject)
        assertEquals(setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL), payload.channels)
        assertEquals(classId.toString(), payload.data["classId"])
    }

    @Test
    fun `fromBookingCancelled creates push-only payload`() {
        val classId = UUID.randomUUID()
        val event = BookingCancelledEvent(userId, classId, Instant.now())

        val payload = factory.fromBookingCancelled(event)

        assertEquals("Booking cancelled", payload.subject)
        assertEquals(setOf(NotificationChannel.PUSH), payload.channels)
    }

    @Test
    fun `fromWaitlistPromoted creates push and email payload`() {
        val classId = UUID.randomUUID()
        val event = WaitlistPromotedEvent(userId, classId, "Morning Yoga", Instant.now())

        val payload = factory.fromWaitlistPromoted(event)

        assertEquals("You're in! Waitlist promotion", payload.subject)
        assertEquals(setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL), payload.channels)
        assertTrue(payload.body.contains("Morning Yoga"))
    }

    @Test
    fun `fromSubscriptionCreated creates push and email payload`() {
        val subscriptionId = UUID.randomUUID()
        val event = SubscriptionCreatedEvent(subscriptionId, userId, "premium", "sub_123", Instant.now().plusSeconds(86400))

        val payload = factory.fromSubscriptionCreated(event)

        assertEquals("Subscription confirmed", payload.subject)
        assertEquals(setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL), payload.channels)
        assertTrue(payload.body.contains("premium"))
    }

    @Test
    fun `fromSubscriptionRenewed creates push and email payload`() {
        val subscriptionId = UUID.randomUUID()
        val event = SubscriptionRenewedEvent(subscriptionId, userId, Instant.now(), "MONTHLY")

        val payload = factory.fromSubscriptionRenewed(event)

        assertEquals("Subscription renewed", payload.subject)
        assertEquals(setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL), payload.channels)
    }

    @Test
    fun `fromSubscriptionCancelled creates push and email payload`() {
        val subscriptionId = UUID.randomUUID()
        val event = SubscriptionCancelledEvent(subscriptionId, userId, Instant.now())

        val payload = factory.fromSubscriptionCancelled(event)

        assertEquals("Subscription cancelled", payload.subject)
        assertEquals(setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL), payload.channels)
    }

    @Test
    fun `fromSubscriptionExpired creates push and email payload`() {
        val subscriptionId = UUID.randomUUID()
        val event = SubscriptionExpiredEvent(subscriptionId, userId)

        val payload = factory.fromSubscriptionExpired(event)

        assertEquals("Subscription expired", payload.subject)
        assertEquals(setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL), payload.channels)
    }
}
