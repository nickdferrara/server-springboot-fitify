package com.nickdferrara.fitify.notification.internal.service

import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.identity.PasswordResetRequestedEvent
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassBookedEvent
import com.nickdferrara.fitify.scheduling.ClassFullEvent
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import com.nickdferrara.fitify.subscription.SubscriptionRenewedEvent
import org.springframework.stereotype.Component

internal data class NotificationPayload(
    val subject: String,
    val body: String,
    val channels: Set<NotificationChannel>,
    val data: Map<String, String> = emptyMap(),
)

@Component
internal class NotificationPayloadFactory {

    fun fromUserRegistered(event: UserRegisteredEvent) = NotificationPayload(
        subject = "Welcome to Fitify!",
        body = "Hi ${event.firstName}, welcome to Fitify! We're excited to have you on board.",
        channels = setOf(NotificationChannel.EMAIL),
        data = mapOf("email" to event.email),
    )

    fun fromPasswordResetRequested(event: PasswordResetRequestedEvent) = NotificationPayload(
        subject = "Reset your password",
        body = "Click the following link to reset your password: ${event.resetLink}. It expires at ${event.expiresAt}.",
        channels = setOf(NotificationChannel.EMAIL),
        data = mapOf("email" to event.email),
    )

    fun fromClassBooked(event: ClassBookedEvent) = NotificationPayload(
        subject = "Booking confirmed",
        body = "Your booking for ${event.className} at ${event.startTime} has been confirmed.",
        channels = setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
        data = mapOf("classId" to event.classId.toString()),
    )

    fun fromBookingCancelled(event: BookingCancelledEvent) = NotificationPayload(
        subject = "Booking cancelled",
        body = "Your booking has been cancelled.",
        channels = setOf(NotificationChannel.PUSH),
        data = mapOf("classId" to event.classId.toString()),
    )

    fun fromWaitlistPromoted(event: WaitlistPromotedEvent) = NotificationPayload(
        subject = "You're in! Waitlist promotion",
        body = "Great news! A spot opened up for ${event.className} at ${event.startTime}. You've been moved off the waitlist.",
        channels = setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
        data = mapOf("classId" to event.classId.toString()),
    )

    fun fromSubscriptionCreated(event: SubscriptionCreatedEvent) = NotificationPayload(
        subject = "Subscription confirmed",
        body = "Your ${event.planType} subscription has been activated.",
        channels = setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
        data = mapOf("subscriptionId" to event.subscriptionId.toString()),
    )

    fun fromSubscriptionRenewed(event: SubscriptionRenewedEvent) = NotificationPayload(
        subject = "Subscription renewed",
        body = "Your subscription has been renewed. Next billing date: ${event.newPeriodEnd}.",
        channels = setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
        data = mapOf("subscriptionId" to event.subscriptionId.toString()),
    )

    fun fromSubscriptionCancelled(event: SubscriptionCancelledEvent) = NotificationPayload(
        subject = "Subscription cancelled",
        body = "Your subscription has been cancelled. It remains active until ${event.effectiveDate}.",
        channels = setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
        data = mapOf("subscriptionId" to event.subscriptionId.toString()),
    )

    fun fromSubscriptionExpired(event: SubscriptionExpiredEvent) = NotificationPayload(
        subject = "Subscription expired",
        body = "Your subscription has expired. Renew to continue accessing Fitify services.",
        channels = setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
        data = mapOf("subscriptionId" to event.subscriptionId.toString()),
    )

    fun fromLocationDeactivated(event: LocationDeactivatedEvent) = NotificationPayload(
        subject = "Location update",
        body = "A location you follow has been deactivated effective ${event.effectiveDate}.",
        channels = setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
        data = mapOf("locationId" to event.locationId.toString()),
    )

    fun fromLocationUpdated(event: LocationUpdatedEvent) = NotificationPayload(
        subject = "Location changed",
        body = "A location you follow has been updated: ${event.updatedFields.joinToString(", ")}.",
        channels = setOf(NotificationChannel.PUSH, NotificationChannel.EMAIL),
        data = mapOf("locationId" to event.locationId.toString()),
    )
}
