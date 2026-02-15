package com.nickdferrara.fitify.subscription.internal

import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import com.nickdferrara.fitify.subscription.internal.service.SubscriptionEventListener
import org.junit.jupiter.api.Test
import java.util.UUID

class SubscriptionEventListenerTest {

    private val listener = SubscriptionEventListener()

    @Test
    fun `onBusinessRuleUpdated handles grace period change`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "subscription_grace_period_days",
            newValue = "7",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)
    }

    @Test
    fun `onBusinessRuleUpdated handles discount stacking change`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "discount_stacking_enabled",
            newValue = "true",
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

    @Test
    fun `onUserRegistered handles event without error`() {
        val event = UserRegisteredEvent(
            userId = UUID.randomUUID(),
            email = "new@example.com",
            firstName = "Jane",
            lastName = "Doe",
        )

        listener.onUserRegistered(event)
    }
}
