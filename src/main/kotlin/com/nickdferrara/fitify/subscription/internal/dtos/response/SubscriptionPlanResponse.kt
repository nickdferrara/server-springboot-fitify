package com.nickdferrara.fitify.subscription.internal.dtos.response

import java.math.BigDecimal
import java.util.UUID

internal data class SubscriptionPlanResponse(
    val id: UUID,
    val name: String,
    val planType: String,
    val basePrice: BigDecimal,
)
