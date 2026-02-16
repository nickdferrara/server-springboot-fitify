package com.nickdferrara.fitify.admin.internal.dtos.response

import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import java.math.BigDecimal

internal data class MetricSummary(
    val metricType: MetricType,
    val currentValue: BigDecimal,
    val trend: TrendComparison?,
)
