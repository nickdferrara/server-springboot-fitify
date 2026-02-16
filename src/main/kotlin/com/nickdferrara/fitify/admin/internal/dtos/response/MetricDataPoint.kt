package com.nickdferrara.fitify.admin.internal.dtos.response

import java.math.BigDecimal
import java.time.LocalDate

internal data class MetricDataPoint(
    val date: LocalDate,
    val value: BigDecimal,
    val dimensions: Map<String, String> = emptyMap(),
)
