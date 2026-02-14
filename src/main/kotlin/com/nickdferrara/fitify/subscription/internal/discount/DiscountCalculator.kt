package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import java.math.BigDecimal

internal interface DiscountCalculator {
    val strategyType: String
    fun applies(context: DiscountContext, strategy: DiscountStrategy): Boolean
    fun calculate(context: DiscountContext, strategy: DiscountStrategy): BigDecimal
}
