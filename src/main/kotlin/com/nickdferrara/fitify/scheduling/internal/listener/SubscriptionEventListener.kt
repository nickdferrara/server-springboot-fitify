package com.nickdferrara.fitify.scheduling.internal.listener

import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class SubscriptionEventListener {

    private val logger = LoggerFactory.getLogger(SubscriptionEventListener::class.java)

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
}
