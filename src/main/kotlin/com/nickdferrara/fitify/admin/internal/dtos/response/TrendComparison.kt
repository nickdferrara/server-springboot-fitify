package com.nickdferrara.fitify.admin.internal.dtos.response

import java.math.BigDecimal

internal data class TrendComparison(
    val currentPeriodTotal: BigDecimal,
    val previousPeriodTotal: BigDecimal,
    val changeAbsolute: BigDecimal,
    val changePercent: BigDecimal?,
)
