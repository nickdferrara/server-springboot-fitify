package com.nickdferrara.fitify.admin.internal.dtos.response

import java.time.LocalDate
import java.util.UUID

internal data class OverviewResponse(
    val from: LocalDate,
    val to: LocalDate,
    val locationId: UUID?,
    val metrics: List<MetricSummary>,
)
