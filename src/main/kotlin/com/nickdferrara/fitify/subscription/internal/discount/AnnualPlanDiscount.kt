package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
internal class AnnualPlanDiscount : DiscountCalculator {

    override val strategyType: String = "ANNUAL_PLAN"

    override fun applies(context: DiscountContext, strategy: DiscountStrategy): Boolean {
        return context.planType == PlanType.ANNUAL
    }

    override fun calculate(context: DiscountContext, strategy: DiscountStrategy): BigDecimal {
        return strategy.value
    }
}
