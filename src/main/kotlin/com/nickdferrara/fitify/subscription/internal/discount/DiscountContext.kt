package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import java.math.BigDecimal
import java.util.UUID

internal data class DiscountContext(
    val userId: UUID,
    val planType: PlanType,
    val basePrice: BigDecimal,
    val promotionalCode: String? = null,
    val subscriptionMonths: Long = 0,
)
