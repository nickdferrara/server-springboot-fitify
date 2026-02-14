package com.nickdferrara.fitify.subscription

import java.time.Instant
import java.util.UUID

data class SubscriptionCancelledEvent(
    val subscriptionId: UUID,
    val userId: UUID,
    val effectiveDate: Instant,
)
