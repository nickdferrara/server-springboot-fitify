package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.util.UUID

internal class LoyaltyTierDiscountTest {

    private val discount = LoyaltyTierDiscount()

    private fun buildStrategy(minMonths: Any? = 12) = DiscountStrategy(
        strategyType = "LOYALTY_TIER",
        value = BigDecimal("5.00"),
        conditions = if (minMonths != null) mapOf("min_months" to minMonths) else emptyMap(),
    )

    private fun buildContext(months: Long) = DiscountContext(
        userId = UUID.randomUUID(),
        planType = PlanType.MONTHLY,
        basePrice = BigDecimal("100.00"),
        subscriptionMonths = months,
    )

    @ParameterizedTest
    @CsvSource(
        "12, true",
        "13, true",
        "24, true",
        "11, false",
        "0, false",
    )
    fun `applies checks subscription months against min_months boundary`(months: Long, expected: Boolean) {
        val context = buildContext(months)
        val strategy = buildStrategy(minMonths = 12)

        assertThat(discount.applies(context, strategy)).isEqualTo(expected)
    }

    @Test
    fun `applies returns false when min_months condition is missing`() {
        val context = buildContext(24)
        val strategy = buildStrategy(minMonths = null)

        assertThat(discount.applies(context, strategy)).isFalse()
    }

    @Test
    fun `applies returns false when min_months is not numeric`() {
        val context = buildContext(24)
        val strategy = DiscountStrategy(
            strategyType = "LOYALTY_TIER",
            value = BigDecimal("5.00"),
            conditions = mapOf("min_months" to "twelve"),
        )

        assertThat(discount.applies(context, strategy)).isFalse()
    }

    @Test
    fun `calculate returns strategy value`() {
        val context = buildContext(12)
        val strategy = buildStrategy()

        assertThat(discount.calculate(context, strategy)).isEqualByComparingTo(BigDecimal("5.00"))
    }
}
