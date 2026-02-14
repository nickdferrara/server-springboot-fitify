package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
internal class LoyaltyTierDiscount : DiscountCalculator {

    override val strategyType: String = "LOYALTY_TIER"

    override fun applies(context: DiscountContext, strategy: DiscountStrategy): Boolean {
        val minMonths = (strategy.conditions["min_months"] as? Number)?.toLong() ?: return false
        return context.subscriptionMonths >= minMonths
    }

    override fun calculate(context: DiscountContext, strategy: DiscountStrategy): BigDecimal {
        return strategy.value
    }
}
