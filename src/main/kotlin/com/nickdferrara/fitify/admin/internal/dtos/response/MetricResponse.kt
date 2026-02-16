package com.nickdferrara.fitify.admin.internal.dtos.response

import com.nickdferrara.fitify.admin.internal.entities.enums.Granularity
import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import java.time.LocalDate

internal data class MetricResponse(
    val metricType: MetricType,
    val granularity: Granularity,
    val from: LocalDate,
    val to: LocalDate,
    val dataPoints: List<MetricDataPoint>,
    val trend: TrendComparison?,
)
