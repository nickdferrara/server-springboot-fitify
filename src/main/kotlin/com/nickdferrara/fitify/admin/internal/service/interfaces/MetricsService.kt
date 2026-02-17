package com.nickdferrara.fitify.admin.internal.service.interfaces

import com.nickdferrara.fitify.admin.internal.dtos.response.MetricResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.OverviewResponse
import com.nickdferrara.fitify.admin.internal.entities.enums.Granularity
import java.time.LocalDate
import java.util.UUID

internal interface MetricsService {
    fun getSignups(from: LocalDate, to: LocalDate, granularity: Granularity, locationId: UUID?): MetricResponse
    fun getCancellations(from: LocalDate, to: LocalDate, granularity: Granularity, locationId: UUID?): MetricResponse
    fun getRevenue(from: LocalDate, to: LocalDate, granularity: Granularity, locationId: UUID?): MetricResponse
    fun getOverview(from: LocalDate, to: LocalDate, locationId: UUID?): OverviewResponse
}
