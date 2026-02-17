package com.nickdferrara.fitify.scheduling.internal.listener

import com.nickdferrara.fitify.scheduling.internal.service.interfaces.SchedulingCommandService
import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
internal class BusinessRuleEventListener(
    private val schedulingCommandService: SchedulingCommandService,
) {

    private val logger = LoggerFactory.getLogger(BusinessRuleEventListener::class.java)

    @EventListener
    fun onBusinessRuleUpdated(event: BusinessRuleUpdatedEvent) {
        when (event.ruleKey) {
            "cancellation_window_hours" -> {
                schedulingCommandService.cancellationWindowHours = event.newValue.toLong()
                logger.info("Updated cancellationWindowHours to {}", event.newValue)
            }
            "max_waitlist_size" -> {
                schedulingCommandService.maxWaitlistSize = event.newValue.toInt()
                logger.info("Updated maxWaitlistSize to {}", event.newValue)
            }
            "max_bookings_per_user_per_day" -> {
                schedulingCommandService.maxBookingsPerDay = event.newValue.toInt()
                logger.info("Updated maxBookingsPerDay to {}", event.newValue)
            }
        }
    }
}
