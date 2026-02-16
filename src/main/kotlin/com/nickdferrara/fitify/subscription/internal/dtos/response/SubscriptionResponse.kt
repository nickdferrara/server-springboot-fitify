package com.nickdferrara.fitify.subscription.internal.dtos.response

import java.time.Instant
import java.util.UUID

internal data class SubscriptionResponse(
    val id: UUID,
    val userId: UUID,
    val planType: String,
    val status: String,
    val currentPeriodStart: Instant?,
    val currentPeriodEnd: Instant?,
    val expiresAt: Instant?,
    val createdAt: Instant?,
)
