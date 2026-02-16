package com.nickdferrara.fitify.admin.internal.listener

import com.nickdferrara.fitify.admin.internal.entities.MetricsSnapshot
import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.admin.internal.repository.MetricsSnapshotRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Component
internal class MetricsRecorder(
    private val metricsSnapshotRepository: MetricsSnapshotRepository,
) {

    fun incrementMetric(
        metricType: MetricType,
        locationId: UUID?,
        dimensions: Map<String, String>,
    ) {
        val today = LocalDate.now()
        val existing = findSnapshot(metricType, today, locationId, dimensions)
        if (existing != null) {
            existing.value = existing.value.add(BigDecimal.ONE)
            metricsSnapshotRepository.save(existing)
        } else {
            metricsSnapshotRepository.save(
                MetricsSnapshot(
                    metricType = metricType,
                    locationId = locationId,
                    dimensions = dimensions,
                    value = BigDecimal.ONE,
                    snapshotDate = today,
                )
            )
        }
    }

    private fun findSnapshot(
        metricType: MetricType,
        date: LocalDate,
        locationId: UUID?,
        dimensions: Map<String, String>,
    ): MetricsSnapshot? {
        val snapshots = if (locationId != null) {
            metricsSnapshotRepository.findByMetricTypeAndSnapshotDateAndLocationId(metricType, date, locationId)
        } else {
            metricsSnapshotRepository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(metricType, date)
        }
        return snapshots.find { it.dimensions == dimensions }
    }
}
