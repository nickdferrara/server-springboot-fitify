package com.nickdferrara.fitify.scheduling.internal.listener

import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class SubscriptionEventListenerTest {

    private val listener = SubscriptionEventListener()

    @Test
    fun `onSubscriptionCancelled handles event without error`() {
        val event = SubscriptionCancelledEvent(
            subscriptionId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            effectiveDate = Instant.now(),
        )

        listener.onSubscriptionCancelled(event)
    }

    @Test
    fun `onSubscriptionExpired handles event without error`() {
        val event = SubscriptionExpiredEvent(
            subscriptionId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
        )

        listener.onSubscriptionExpired(event)
    }
}
