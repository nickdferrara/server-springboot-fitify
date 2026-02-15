package com.nickdferrara.fitify.subscription

import java.time.Instant
import java.util.UUID

data class SubscriptionCreatedEvent(
    val subscriptionId: UUID,
    val userId: UUID,
    val planType: String,
    val stripeSubscriptionId: String,
    val expiresAt: Instant,
)
