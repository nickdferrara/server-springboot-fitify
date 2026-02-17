package com.nickdferrara.fitify.notification.internal.listener

import com.nickdferrara.fitify.identity.PasswordResetRequestedEvent
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import com.nickdferrara.fitify.notification.internal.factory.NotificationPayloadFactory
import com.nickdferrara.fitify.notification.internal.service.interfaces.NotificationService
import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassBookedEvent
import com.nickdferrara.fitify.scheduling.ClassFullEvent
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import com.nickdferrara.fitify.subscription.SubscriptionRenewedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
internal class NotificationEventListener(
    private val notificationService: NotificationService,
    private val payloadFactory: NotificationPayloadFactory,
) {

    private val logger = LoggerFactory.getLogger(NotificationEventListener::class.java)

    @Async("notificationExecutor")
    @EventListener
    fun onUserRegistered(event: UserRegisteredEvent) {
        val payload = payloadFactory.fromUserRegistered(event)
        notificationService.sendNotification(event.userId, "UserRegisteredEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onPasswordResetRequested(event: PasswordResetRequestedEvent) {
        val payload = payloadFactory.fromPasswordResetRequested(event)
        notificationService.sendNotification(event.userId, "PasswordResetRequestedEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onClassBooked(event: ClassBookedEvent) {
        val payload = payloadFactory.fromClassBooked(event)
        notificationService.sendNotification(event.userId, "ClassBookedEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onBookingCancelled(event: BookingCancelledEvent) {
        val payload = payloadFactory.fromBookingCancelled(event)
        notificationService.sendNotification(event.userId, "BookingCancelledEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onWaitlistPromoted(event: WaitlistPromotedEvent) {
        val payload = payloadFactory.fromWaitlistPromoted(event)
        notificationService.sendNotification(event.userId, "WaitlistPromotedEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onClassFull(event: ClassFullEvent) {
        logger.info("Class {} is full with {} on waitlist", event.className, event.waitlistSize)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onSubscriptionCreated(event: SubscriptionCreatedEvent) {
        val payload = payloadFactory.fromSubscriptionCreated(event)
        notificationService.sendNotification(event.userId, "SubscriptionCreatedEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onSubscriptionRenewed(event: SubscriptionRenewedEvent) {
        val payload = payloadFactory.fromSubscriptionRenewed(event)
        notificationService.sendNotification(event.userId, "SubscriptionRenewedEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onSubscriptionCancelled(event: SubscriptionCancelledEvent) {
        val payload = payloadFactory.fromSubscriptionCancelled(event)
        notificationService.sendNotification(event.userId, "SubscriptionCancelledEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onSubscriptionExpired(event: SubscriptionExpiredEvent) {
        val payload = payloadFactory.fromSubscriptionExpired(event)
        notificationService.sendNotification(event.userId, "SubscriptionExpiredEvent", payload)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onLocationDeactivated(event: LocationDeactivatedEvent) {
        logger.info("Location {} deactivated, notification requires user lookup", event.locationId)
    }

    @Async("notificationExecutor")
    @EventListener
    fun onLocationUpdated(event: LocationUpdatedEvent) {
        logger.info("Location {} updated: {}, notification requires user lookup", event.locationId, event.updatedFields)
    }
}
