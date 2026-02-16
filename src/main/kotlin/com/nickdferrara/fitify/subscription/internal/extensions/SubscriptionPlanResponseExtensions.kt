package com.nickdferrara.fitify.subscription.internal.extensions

import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionPlanResponse
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionPlan

internal fun SubscriptionPlan.toResponse() = SubscriptionPlanResponse(
    id = id!!,
    name = name,
    planType = planType.name,
    basePrice = basePrice,
)
