package com.nickdferrara.fitify.admin.internal.repository

import com.nickdferrara.fitify.admin.internal.entities.MetricType
import com.nickdferrara.fitify.admin.internal.entities.MetricsSnapshot
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

internal interface MetricsSnapshotRepository : JpaRepository<MetricsSnapshot, UUID> {

    fun findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(
        metricType: MetricType,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<MetricsSnapshot>

    fun findByMetricTypeAndSnapshotDateBetweenAndLocationId(
        metricType: MetricType,
        startDate: LocalDate,
        endDate: LocalDate,
        locationId: UUID,
    ): List<MetricsSnapshot>

    fun findByMetricTypeAndSnapshotDateAndLocationIdIsNull(
        metricType: MetricType,
        snapshotDate: LocalDate,
    ): List<MetricsSnapshot>

    fun findByMetricTypeAndSnapshotDateAndLocationId(
        metricType: MetricType,
        snapshotDate: LocalDate,
        locationId: UUID,
    ): List<MetricsSnapshot>
}
