package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal
import java.util.UUID

internal class AnnualPlanDiscountTest {

    private val discount = AnnualPlanDiscount()

    private fun buildStrategy(value: BigDecimal = BigDecimal("10.00")) = DiscountStrategy(
        strategyType = "ANNUAL_PLAN",
        value = value,
        conditions = emptyMap(),
    )

    private fun buildContext(planType: PlanType) = DiscountContext(
        userId = UUID.randomUUID(),
        planType = planType,
        basePrice = BigDecimal("100.00"),
    )

    @ParameterizedTest
    @EnumSource(PlanType::class)
    fun `applies returns true only for ANNUAL plan type`(planType: PlanType) {
        val context = buildContext(planType)
        val strategy = buildStrategy()

        val result = discount.applies(context, strategy)

        assertThat(result).isEqualTo(planType == PlanType.ANNUAL)
    }

    @ParameterizedTest
    @EnumSource(PlanType::class)
    fun `calculate returns strategy value regardless of plan type`(planType: PlanType) {
        val context = buildContext(planType)
        val strategy = buildStrategy(BigDecimal("15.50"))

        val result = discount.calculate(context, strategy)

        assertThat(result).isEqualByComparingTo(BigDecimal("15.50"))
    }
}
