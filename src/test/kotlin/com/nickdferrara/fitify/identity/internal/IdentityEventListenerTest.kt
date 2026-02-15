package com.nickdferrara.fitify.identity.internal

import com.nickdferrara.fitify.identity.internal.listener.IdentityEventListener
import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import org.junit.jupiter.api.Test

class IdentityEventListenerTest {

    private val listener = IdentityEventListener()

    @Test
    fun `onBusinessRuleUpdated handles password policy change`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "password_min_length",
            newValue = "12",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)
    }

    @Test
    fun `onBusinessRuleUpdated handles session timeout change`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "session_timeout_minutes",
            newValue = "60",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)
    }

    @Test
    fun `onBusinessRuleUpdated handles password complexity change`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "password_complexity",
            newValue = "high",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)
    }

    @Test
    fun `onBusinessRuleUpdated ignores unrelated rules`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "max_waitlist_size",
            newValue = "30",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)
    }
}
