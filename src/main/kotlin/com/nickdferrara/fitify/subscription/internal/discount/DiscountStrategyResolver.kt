package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.repository.DiscountStrategyRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
internal class DiscountStrategyResolver(
    private val discountStrategyRepository: DiscountStrategyRepository,
    private val calculators: List<DiscountCalculator>,
) {

    fun resolveDiscount(context: DiscountContext): BigDecimal {
        val strategies = discountStrategyRepository.findByActiveTrueOrderByPriorityAsc()
        val calculatorsByType = calculators.associateBy { it.strategyType }

        var totalDiscount = BigDecimal.ZERO

        for (strategy in strategies) {
            val calculator = calculatorsByType[strategy.strategyType] ?: continue
            if (calculator.applies(context, strategy)) {
                totalDiscount = totalDiscount.add(calculator.calculate(context, strategy))
            }
        }

        return totalDiscount.min(context.basePrice)
    }
}
