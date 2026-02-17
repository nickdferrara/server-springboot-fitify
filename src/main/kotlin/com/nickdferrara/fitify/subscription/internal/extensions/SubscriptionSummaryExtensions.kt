package com.nickdferrara.fitify.subscription.internal.extensions

import com.nickdferrara.fitify.subscription.SubscriptionSummary
import com.nickdferrara.fitify.subscription.internal.entities.Subscription

internal fun Subscription.toSummary() = SubscriptionSummary(
    id = id!!,
    userId = userId,
    planType = planType.name,
    status = status.name,
    currentPeriodEnd = currentPeriodEnd,
)
