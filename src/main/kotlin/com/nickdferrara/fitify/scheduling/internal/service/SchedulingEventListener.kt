package com.nickdferrara.fitify.scheduling.internal.service

import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

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
}
