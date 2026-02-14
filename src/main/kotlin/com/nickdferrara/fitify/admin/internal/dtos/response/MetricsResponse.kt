package com.nickdferrara.fitify.admin.internal.dtos.response

import com.nickdferrara.fitify.admin.internal.entities.Granularity
import com.nickdferrara.fitify.admin.internal.entities.MetricType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal data class MetricDataPoint(
    val date: LocalDate,
    val value: BigDecimal,
    val dimensions: Map<String, String> = emptyMap(),
)

internal data class TrendComparison(
    val currentPeriodTotal: BigDecimal,
    val previousPeriodTotal: BigDecimal,
    val changeAbsolute: BigDecimal,
    val changePercent: BigDecimal?,
)

internal data class MetricResponse(
    val metricType: MetricType,
    val granularity: Granularity,
    val from: LocalDate,
    val to: LocalDate,
    val dataPoints: List<MetricDataPoint>,
    val trend: TrendComparison?,
)

internal data class MetricSummary(
    val metricType: MetricType,
    val currentValue: BigDecimal,
    val trend: TrendComparison?,
)

internal data class OverviewResponse(
    val from: LocalDate,
    val to: LocalDate,
    val locationId: UUID?,
    val metrics: List<MetricSummary>,
)
