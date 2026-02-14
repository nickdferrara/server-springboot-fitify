package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
internal class PromotionalCodeDiscount : DiscountCalculator {

    override val strategyType: String = "PROMOTIONAL_CODE"

    override fun applies(context: DiscountContext, strategy: DiscountStrategy): Boolean {
        val validCodes = strategy.conditions["codes"]
        if (validCodes !is List<*> || context.promotionalCode == null) return false
        return validCodes.contains(context.promotionalCode)
    }

    override fun calculate(context: DiscountContext, strategy: DiscountStrategy): BigDecimal {
        return strategy.value
    }
}
