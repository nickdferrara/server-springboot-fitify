package com.nickdferrara.fitify.subscription

import java.time.Instant
import java.util.UUID

data class SubscriptionRenewedEvent(
    val subscriptionId: UUID,
    val userId: UUID,
    val newPeriodEnd: Instant,
    val planType: String,
)
