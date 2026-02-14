package com.nickdferrara.fitify.subscription.internal.dtos.response

import com.nickdferrara.fitify.subscription.internal.entities.Subscription
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

internal fun Subscription.toResponse() = SubscriptionResponse(
    id = id!!,
    userId = userId,
    planType = planType.name,
    status = status.name,
    currentPeriodStart = currentPeriodStart,
    currentPeriodEnd = currentPeriodEnd,
    expiresAt = expiresAt,
    createdAt = createdAt,
)
