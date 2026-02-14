package com.nickdferrara.fitify.subscription.internal.service

import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
internal class SubscriptionEventListener {

    private val logger = LoggerFactory.getLogger(SubscriptionEventListener::class.java)

    @EventListener
    fun onBusinessRuleUpdated(event: BusinessRuleUpdatedEvent) {
        when (event.ruleKey) {
            "subscription_grace_period_days" -> {
                logger.info("Business rule updated: subscription_grace_period_days = {}", event.newValue)
            }
            "discount_stacking_enabled" -> {
                logger.info("Business rule updated: discount_stacking_enabled = {}", event.newValue)
            }
        }
    }
}
