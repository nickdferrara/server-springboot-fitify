package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.util.UUID

internal class PromotionalCodeDiscountTest {

    private val discount = PromotionalCodeDiscount()

    private fun buildStrategy(codes: List<String> = listOf("SAVE10", "WELCOME")) = DiscountStrategy(
        strategyType = "PROMOTIONAL_CODE",
        value = BigDecimal("10.00"),
        conditions = mapOf("codes" to codes),
    )

    private fun buildContext(promoCode: String? = null) = DiscountContext(
        userId = UUID.randomUUID(),
        planType = PlanType.MONTHLY,
        basePrice = BigDecimal("100.00"),
        promotionalCode = promoCode,
    )

    @ParameterizedTest
    @CsvSource(
        "SAVE10, true",
        "WELCOME, true",
        "INVALID, false",
        ", false",
    )
    fun `applies checks promotional code against valid codes list`(code: String?, expected: Boolean) {
        val context = buildContext(code)
        val strategy = buildStrategy()

        val result = discount.applies(context, strategy)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `applies returns false when codes condition is missing`() {
        val context = buildContext("SAVE10")
        val strategy = DiscountStrategy(
            strategyType = "PROMOTIONAL_CODE",
            value = BigDecimal("10.00"),
            conditions = emptyMap(),
        )

        assertThat(discount.applies(context, strategy)).isFalse()
    }

    @Test
    fun `applies returns false when codes condition is not a list`() {
        val context = buildContext("SAVE10")
        val strategy = DiscountStrategy(
            strategyType = "PROMOTIONAL_CODE",
            value = BigDecimal("10.00"),
            conditions = mapOf("codes" to "SAVE10"),
        )

        assertThat(discount.applies(context, strategy)).isFalse()
    }

    @Test
    fun `calculate returns strategy value`() {
        val context = buildContext("SAVE10")
        val strategy = buildStrategy()

        assertThat(discount.calculate(context, strategy)).isEqualByComparingTo(BigDecimal("10.00"))
    }
}
