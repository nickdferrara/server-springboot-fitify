package com.nickdferrara.fitify.subscription.internal.discount

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import java.util.stream.Stream

internal class SeasonalCampaignDiscountTest {

    private val discount = SeasonalCampaignDiscount()

    private fun buildContext() = DiscountContext(
        userId = UUID.randomUUID(),
        planType = PlanType.MONTHLY,
        basePrice = BigDecimal("100.00"),
    )

    private fun buildStrategy(startDate: String?, endDate: String?): DiscountStrategy {
        val conditions = mutableMapOf<String, Any>()
        if (startDate != null) conditions["start_date"] = startDate
        if (endDate != null) conditions["end_date"] = endDate
        return DiscountStrategy(
            strategyType = "SEASONAL_CAMPAIGN",
            value = BigDecimal("20.00"),
            conditions = conditions,
        )
    }

    @ParameterizedTest
    @MethodSource("dateRangeProvider")
    fun `applies checks if today falls within campaign date range`(
        startOffset: Long,
        endOffset: Long,
        expected: Boolean,
    ) {
        val today = LocalDate.now()
        val strategy = buildStrategy(
            startDate = today.plusDays(startOffset).toString(),
            endDate = today.plusDays(endOffset).toString(),
        )

        assertThat(discount.applies(buildContext(), strategy)).isEqualTo(expected)
    }

    @Test
    fun `applies returns false when start_date is missing`() {
        val strategy = buildStrategy(startDate = null, endDate = LocalDate.now().plusDays(5).toString())
        assertThat(discount.applies(buildContext(), strategy)).isFalse()
    }

    @Test
    fun `applies returns false when end_date is missing`() {
        val strategy = buildStrategy(startDate = LocalDate.now().minusDays(5).toString(), endDate = null)
        assertThat(discount.applies(buildContext(), strategy)).isFalse()
    }

    @Test
    fun `applies returns false when date values are non-string types`() {
        val strategy = DiscountStrategy(
            strategyType = "SEASONAL_CAMPAIGN",
            value = BigDecimal("20.00"),
            conditions = mapOf("start_date" to 12345, "end_date" to 67890),
        )
        assertThat(discount.applies(buildContext(), strategy)).isFalse()
    }

    @Test
    fun `calculate returns strategy value`() {
        val strategy = buildStrategy(
            startDate = LocalDate.now().minusDays(1).toString(),
            endDate = LocalDate.now().plusDays(1).toString(),
        )
        assertThat(discount.calculate(buildContext(), strategy)).isEqualByComparingTo(BigDecimal("20.00"))
    }

    companion object {
        @JvmStatic
        fun dateRangeProvider(): Stream<Arguments> = Stream.of(
            // today is within range (started yesterday, ends tomorrow)
            Arguments.of(-1L, 1L, true),
            // today is the start date
            Arguments.of(0L, 5L, true),
            // today is the end date
            Arguments.of(-5L, 0L, true),
            // campaign starts tomorrow (not yet active)
            Arguments.of(1L, 10L, false),
            // campaign ended yesterday
            Arguments.of(-10L, -1L, false),
        )
    }
}
