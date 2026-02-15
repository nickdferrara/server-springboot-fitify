package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import com.nickdferrara.fitify.subscription.internal.repository.DiscountStrategyRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

internal class DiscountStrategyResolverTest {

    private val repository = mockk<DiscountStrategyRepository>()

    private val annualPlanDiscount = AnnualPlanDiscount()
    private val loyaltyTierDiscount = LoyaltyTierDiscount()
    private val promotionalCodeDiscount = PromotionalCodeDiscount()

    private val resolver = DiscountStrategyResolver(
        discountStrategyRepository = repository,
        calculators = listOf(annualPlanDiscount, loyaltyTierDiscount, promotionalCodeDiscount),
    )

    private fun buildContext(
        planType: PlanType = PlanType.ANNUAL,
        basePrice: BigDecimal = BigDecimal("100.00"),
        promoCode: String? = null,
        months: Long = 0,
    ) = DiscountContext(
        userId = UUID.randomUUID(),
        planType = planType,
        basePrice = basePrice,
        promotionalCode = promoCode,
        subscriptionMonths = months,
    )

    @Test
    fun `resolveDiscount sums applicable discounts`() {
        val strategies = listOf(
            DiscountStrategy(strategyType = "ANNUAL_PLAN", value = BigDecimal("10.00"), priority = 1),
            DiscountStrategy(
                strategyType = "LOYALTY_TIER",
                value = BigDecimal("5.00"),
                conditions = mapOf("min_months" to 6),
                priority = 2,
            ),
        )
        every { repository.findByActiveTrueOrderByPriorityAsc() } returns strategies

        val context = buildContext(planType = PlanType.ANNUAL, months = 12)
        val result = resolver.resolveDiscount(context)

        assertThat(result).isEqualByComparingTo(BigDecimal("15.00"))
    }

    @Test
    fun `resolveDiscount caps discount at base price`() {
        val strategies = listOf(
            DiscountStrategy(strategyType = "ANNUAL_PLAN", value = BigDecimal("80.00"), priority = 1),
            DiscountStrategy(
                strategyType = "LOYALTY_TIER",
                value = BigDecimal("50.00"),
                conditions = mapOf("min_months" to 6),
                priority = 2,
            ),
        )
        every { repository.findByActiveTrueOrderByPriorityAsc() } returns strategies

        val context = buildContext(planType = PlanType.ANNUAL, basePrice = BigDecimal("100.00"), months = 12)
        val result = resolver.resolveDiscount(context)

        assertThat(result).isEqualByComparingTo(BigDecimal("100.00"))
    }

    @Test
    fun `resolveDiscount returns zero when no strategies match`() {
        val strategies = listOf(
            DiscountStrategy(strategyType = "ANNUAL_PLAN", value = BigDecimal("10.00"), priority = 1),
        )
        every { repository.findByActiveTrueOrderByPriorityAsc() } returns strategies

        val context = buildContext(planType = PlanType.MONTHLY)
        val result = resolver.resolveDiscount(context)

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `resolveDiscount returns zero when no active strategies exist`() {
        every { repository.findByActiveTrueOrderByPriorityAsc() } returns emptyList()

        val context = buildContext()
        val result = resolver.resolveDiscount(context)

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `resolveDiscount skips strategies with no matching calculator`() {
        val strategies = listOf(
            DiscountStrategy(strategyType = "UNKNOWN_TYPE", value = BigDecimal("50.00"), priority = 1),
            DiscountStrategy(strategyType = "ANNUAL_PLAN", value = BigDecimal("10.00"), priority = 2),
        )
        every { repository.findByActiveTrueOrderByPriorityAsc() } returns strategies

        val context = buildContext(planType = PlanType.ANNUAL)
        val result = resolver.resolveDiscount(context)

        assertThat(result).isEqualByComparingTo(BigDecimal("10.00"))
    }
}
