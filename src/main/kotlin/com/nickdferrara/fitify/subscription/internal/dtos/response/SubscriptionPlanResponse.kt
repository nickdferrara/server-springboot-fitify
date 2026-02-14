package com.nickdferrara.fitify.subscription.internal.dtos.response

import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionPlan
import java.math.BigDecimal
import java.util.UUID

internal data class SubscriptionPlanResponse(
    val id: UUID,
    val name: String,
    val planType: String,
    val basePrice: BigDecimal,
)

internal fun SubscriptionPlan.toResponse() = SubscriptionPlanResponse(
    id = id!!,
    name = name,
    planType = planType.name,
    basePrice = basePrice,
)
