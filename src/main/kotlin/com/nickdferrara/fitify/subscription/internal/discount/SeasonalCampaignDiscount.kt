package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate

@Component
internal class SeasonalCampaignDiscount : DiscountCalculator {

    override val strategyType: String = "SEASONAL_CAMPAIGN"

    override fun applies(context: DiscountContext, strategy: DiscountStrategy): Boolean {
        val startDate = (strategy.conditions["start_date"] as? String)?.let { LocalDate.parse(it) } ?: return false
        val endDate = (strategy.conditions["end_date"] as? String)?.let { LocalDate.parse(it) } ?: return false
        val today = LocalDate.now()
        return !today.isBefore(startDate) && !today.isAfter(endDate)
    }

    override fun calculate(context: DiscountContext, strategy: DiscountStrategy): BigDecimal {
        return strategy.value
    }
}
