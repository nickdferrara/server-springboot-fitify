package com.nickdferrara.fitify.identity.internal.listener

import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class IdentityEventListener {

    private val logger = LoggerFactory.getLogger(IdentityEventListener::class.java)

    @TransactionalEventListener
    fun onBusinessRuleUpdated(event: BusinessRuleUpdatedEvent) {
        when (event.ruleKey) {
            "password_min_length", "password_complexity", "session_timeout_minutes" -> {
                logger.info("Identity-related business rule updated: {} = {}", event.ruleKey, event.newValue)
            }
        }
    }
}
