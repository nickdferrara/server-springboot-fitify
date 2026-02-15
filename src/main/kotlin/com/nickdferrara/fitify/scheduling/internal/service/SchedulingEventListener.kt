package com.nickdferrara.fitify.scheduling.internal.service

import com.nickdferrara.fitify.coaching.CoachAssignedEvent
import com.nickdferrara.fitify.coaching.CoachUpdatedEvent
import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class SchedulingEventListener(
    private val schedulingService: SchedulingService,
) {

    private val logger = LoggerFactory.getLogger(SchedulingEventListener::class.java)

    @EventListener
    fun onBusinessRuleUpdated(event: BusinessRuleUpdatedEvent) {
        when (event.ruleKey) {
            "cancellation_window_hours" -> {
                schedulingService.cancellationWindowHours = event.newValue.toLong()
                logger.info("Updated cancellationWindowHours to {}", event.newValue)
            }
            "max_waitlist_size" -> {
                schedulingService.maxWaitlistSize = event.newValue.toInt()
                logger.info("Updated maxWaitlistSize to {}", event.newValue)
            }
            "max_bookings_per_user_per_day" -> {
                schedulingService.maxBookingsPerDay = event.newValue.toInt()
                logger.info("Updated maxBookingsPerDay to {}", event.newValue)
            }
        }
    }

    @TransactionalEventListener
    fun onCoachAssigned(event: CoachAssignedEvent) {
        logger.info("Coach {} assigned to class {}", event.coachId, event.classId)
    }

    @TransactionalEventListener
    fun onCoachUpdated(event: CoachUpdatedEvent) {
        logger.info("Coach {} updated fields: {}", event.coachId, event.updatedFields)
    }

    @TransactionalEventListener
    fun onSubscriptionCancelled(event: SubscriptionCancelledEvent) {
        logger.info(
            "Subscription {} cancelled for user {}, effective {}",
            event.subscriptionId, event.userId, event.effectiveDate,
        )
    }

    @TransactionalEventListener
    fun onSubscriptionExpired(event: SubscriptionExpiredEvent) {
        logger.info(
            "Subscription {} expired for user {}",
            event.subscriptionId, event.userId,
        )
    }

    @TransactionalEventListener
    fun onLocationCreated(event: LocationCreatedEvent) {
        logger.info("New location {} available for scheduling: {}", event.locationId, event.name)
    }

    @TransactionalEventListener
    fun onLocationUpdated(event: LocationUpdatedEvent) {
        logger.info("Location {} updated fields: {}", event.locationId, event.updatedFields)
    }

    @TransactionalEventListener
    fun onLocationDeactivated(event: LocationDeactivatedEvent) {
        logger.info(
            "Location {} deactivated effective {}, flagging affected classes",
            event.locationId, event.effectiveDate,
        )
    }
}
