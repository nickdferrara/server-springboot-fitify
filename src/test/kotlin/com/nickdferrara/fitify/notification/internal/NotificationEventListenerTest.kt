package com.nickdferrara.fitify.notification.internal

import com.nickdferrara.fitify.identity.PasswordResetRequestedEvent
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.notification.internal.service.NotificationEventListener
import com.nickdferrara.fitify.notification.internal.service.NotificationPayload
import com.nickdferrara.fitify.notification.internal.service.NotificationPayloadFactory
import com.nickdferrara.fitify.notification.internal.service.NotificationService
import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassBookedEvent
import com.nickdferrara.fitify.scheduling.ClassFullEvent
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import com.nickdferrara.fitify.subscription.SubscriptionRenewedEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class NotificationEventListenerTest {

    private val notificationService = mockk<NotificationService>(relaxed = true)
    private val payloadFactory = mockk<NotificationPayloadFactory>()

    private val listener = NotificationEventListener(notificationService, payloadFactory)

    private val userId: UUID = UUID.randomUUID()

    private fun buildPayload(subject: String, channels: Set<NotificationChannel> = setOf(NotificationChannel.EMAIL)) =
        NotificationPayload(
            subject = subject,
            body = "Test body",
            channels = channels,
        )

    @Test
    fun `onUserRegistered delegates to service`() {
        val event = UserRegisteredEvent(userId, "test@example.com", "John", "Doe")
        val payload = buildPayload("Welcome to Fitify!")
        every { payloadFactory.fromUserRegistered(event) } returns payload

        listener.onUserRegistered(event)

        verify { notificationService.sendNotification(userId, "UserRegisteredEvent", payload) }
    }

    @Test
    fun `onPasswordResetRequested delegates to service`() {
        val event = PasswordResetRequestedEvent(userId, "test@example.com", "reset-token", 30)
        val payload = buildPayload("Reset your password")
        every { payloadFactory.fromPasswordResetRequested(event) } returns payload

        listener.onPasswordResetRequested(event)

        verify { notificationService.sendNotification(userId, "PasswordResetRequestedEvent", payload) }
    }

    @Test
    fun `onClassBooked delegates to service`() {
        val classId = UUID.randomUUID()
        val event = ClassBookedEvent(userId, classId, "Morning Yoga", Instant.now())
        val payload = buildPayload("Booking confirmed", setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL))
        every { payloadFactory.fromClassBooked(event) } returns payload

        listener.onClassBooked(event)

        verify { notificationService.sendNotification(userId, "ClassBookedEvent", payload) }
    }

    @Test
    fun `onBookingCancelled delegates to service`() {
        val classId = UUID.randomUUID()
        val event = BookingCancelledEvent(userId, classId, Instant.now())
        val payload = buildPayload("Booking cancelled", setOf(NotificationChannel.PUSH))
        every { payloadFactory.fromBookingCancelled(event) } returns payload

        listener.onBookingCancelled(event)

        verify { notificationService.sendNotification(userId, "BookingCancelledEvent", payload) }
    }

    @Test
    fun `onWaitlistPromoted delegates to service`() {
        val classId = UUID.randomUUID()
        val event = WaitlistPromotedEvent(userId, classId, "Morning Yoga", Instant.now())
        val payload = buildPayload("You're in! Waitlist promotion", setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL))
        every { payloadFactory.fromWaitlistPromoted(event) } returns payload

        listener.onWaitlistPromoted(event)

        verify { notificationService.sendNotification(userId, "WaitlistPromotedEvent", payload) }
    }

    @Test
    fun `onClassFull only logs and does not send notification`() {
        val classId = UUID.randomUUID()
        val event = ClassFullEvent(classId, "Morning Yoga", 5)

        listener.onClassFull(event)

        verify(exactly = 0) { notificationService.sendNotification(any(), any(), any()) }
    }

    @Test
    fun `onSubscriptionCreated delegates to service`() {
        val subscriptionId = UUID.randomUUID()
        val event = SubscriptionCreatedEvent(subscriptionId, userId, "premium", "sub_123")
        val payload = buildPayload("Subscription confirmed", setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL))
        every { payloadFactory.fromSubscriptionCreated(event) } returns payload

        listener.onSubscriptionCreated(event)

        verify { notificationService.sendNotification(userId, "SubscriptionCreatedEvent", payload) }
    }

    @Test
    fun `onSubscriptionRenewed delegates to service`() {
        val subscriptionId = UUID.randomUUID()
        val event = SubscriptionRenewedEvent(subscriptionId, userId, Instant.now())
        val payload = buildPayload("Subscription renewed", setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL))
        every { payloadFactory.fromSubscriptionRenewed(event) } returns payload

        listener.onSubscriptionRenewed(event)

        verify { notificationService.sendNotification(userId, "SubscriptionRenewedEvent", payload) }
    }

    @Test
    fun `onSubscriptionCancelled delegates to service`() {
        val subscriptionId = UUID.randomUUID()
        val event = SubscriptionCancelledEvent(subscriptionId, userId, Instant.now())
        val payload = buildPayload("Subscription cancelled", setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL))
        every { payloadFactory.fromSubscriptionCancelled(event) } returns payload

        listener.onSubscriptionCancelled(event)

        verify { notificationService.sendNotification(userId, "SubscriptionCancelledEvent", payload) }
    }

    @Test
    fun `onSubscriptionExpired delegates to service`() {
        val subscriptionId = UUID.randomUUID()
        val event = SubscriptionExpiredEvent(subscriptionId, userId)
        val payload = buildPayload("Subscription expired", setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL))
        every { payloadFactory.fromSubscriptionExpired(event) } returns payload

        listener.onSubscriptionExpired(event)

        verify { notificationService.sendNotification(userId, "SubscriptionExpiredEvent", payload) }
    }
}
