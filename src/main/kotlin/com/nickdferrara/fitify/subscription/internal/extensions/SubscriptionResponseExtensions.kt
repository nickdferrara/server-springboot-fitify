package com.nickdferrara.fitify.subscription.internal.extensions

import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionResponse
import com.nickdferrara.fitify.subscription.internal.entities.Subscription

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
