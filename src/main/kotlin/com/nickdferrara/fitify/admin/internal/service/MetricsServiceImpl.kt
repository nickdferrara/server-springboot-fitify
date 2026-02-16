package com.nickdferrara.fitify.admin.internal.service

import com.nickdferrara.fitify.admin.internal.dtos.response.MetricDataPoint
import com.nickdferrara.fitify.admin.internal.dtos.response.MetricResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.MetricSummary
import com.nickdferrara.fitify.admin.internal.dtos.response.OverviewResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.TrendComparison
import com.nickdferrara.fitify.admin.internal.entities.enums.Granularity
import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.admin.internal.entities.MetricsSnapshot
import com.nickdferrara.fitify.admin.internal.exceptions.InvalidMetricsQueryException
import com.nickdferrara.fitify.admin.internal.repository.MetricsSnapshotRepository
import com.nickdferrara.fitify.admin.internal.service.interfaces.MetricsService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.util.UUID

@Service
internal class MetricsServiceImpl(
    private val metricsSnapshotRepository: MetricsSnapshotRepository,
) : MetricsService {

    override fun getSignups(from: LocalDate, to: LocalDate, granularity: Granularity, locationId: UUID?): MetricResponse {
        validateDateRange(from, to)
        return getMetric(MetricType.SIGNUPS, from, to, granularity, locationId)
    }

    override fun getCancellations(from: LocalDate, to: LocalDate, granularity: Granularity, locationId: UUID?): MetricResponse {
        validateDateRange(from, to)
        return getMetric(MetricType.CANCELLATIONS, from, to, granularity, locationId)
    }

    override fun getRevenue(from: LocalDate, to: LocalDate, granularity: Granularity, locationId: UUID?): MetricResponse {
        validateDateRange(from, to)
        return getMetric(MetricType.REVENUE, from, to, granularity, locationId)
    }

    override fun getOverview(from: LocalDate, to: LocalDate, locationId: UUID?): OverviewResponse {
        validateDateRange(from, to)
        val metricTypes = listOf(
            MetricType.SIGNUPS,
            MetricType.CANCELLATIONS,
            MetricType.REVENUE,
            MetricType.ACTIVE_SUBSCRIPTIONS,
            MetricType.CLASS_UTILIZATION,
            MetricType.WAITLIST_CONVERSION,
            MetricType.CHURN_RATE,
        )

        val summaries = metricTypes.map { metricType ->
            val snapshots = fetchSnapshots(metricType, from, to, locationId)
            val currentValue = computeCurrentValue(metricType, snapshots)
            val trend = computeTrend(metricType, from, to, locationId)
            MetricSummary(
                metricType = metricType,
                currentValue = currentValue,
                trend = trend,
            )
        }

        return OverviewResponse(
            from = from,
            to = to,
            locationId = locationId,
            metrics = summaries,
        )
    }

    private fun getMetric(
        metricType: MetricType,
        from: LocalDate,
        to: LocalDate,
        granularity: Granularity,
        locationId: UUID?,
    ): MetricResponse {
        val snapshots = fetchSnapshots(metricType, from, to, locationId)
        val dataPoints = rollup(snapshots, granularity, metricType)
        val trend = computeTrend(metricType, from, to, locationId)

        return MetricResponse(
            metricType = metricType,
            granularity = granularity,
            from = from,
            to = to,
            dataPoints = dataPoints,
            trend = trend,
        )
    }

    private fun fetchSnapshots(
        metricType: MetricType,
        from: LocalDate,
        to: LocalDate,
        locationId: UUID?,
    ): List<MetricsSnapshot> {
        return if (locationId != null) {
            metricsSnapshotRepository.findByMetricTypeAndSnapshotDateBetweenAndLocationId(
                metricType, from, to, locationId,
            )
        } else {
            metricsSnapshotRepository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(
                metricType, from, to,
            )
        }
    }

    private fun rollup(
        snapshots: List<MetricsSnapshot>,
        granularity: Granularity,
        metricType: MetricType,
    ): List<MetricDataPoint> {
        if (snapshots.isEmpty()) return emptyList()

        val aggregateSnapshots = snapshots.filter { it.dimensions.isEmpty() }

        return when (granularity) {
            Granularity.DAILY -> aggregateSnapshots.map { snapshot ->
                MetricDataPoint(date = snapshot.snapshotDate, value = snapshot.value)
            }.sortedBy { it.date }

            Granularity.WEEKLY -> aggregateSnapshots
                .groupBy {
                    it.snapshotDate.get(IsoFields.WEEK_BASED_YEAR) * 100 +
                        it.snapshotDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                }
                .map { (_, weekSnapshots) ->
                    val weekStart = weekSnapshots.minOf { it.snapshotDate }
                    val value = aggregateValue(weekSnapshots, metricType)
                    MetricDataPoint(date = weekStart, value = value)
                }.sortedBy { it.date }

            Granularity.MONTHLY -> aggregateSnapshots
                .groupBy { it.snapshotDate.year * 100 + it.snapshotDate.monthValue }
                .map { (_, monthSnapshots) ->
                    val monthStart = monthSnapshots.minOf { it.snapshotDate }
                    val value = aggregateValue(monthSnapshots, metricType)
                    MetricDataPoint(date = monthStart, value = value)
                }.sortedBy { it.date }
        }
    }

    private fun aggregateValue(snapshots: List<MetricsSnapshot>, metricType: MetricType): BigDecimal {
        return if (isPointInTimeMetric(metricType)) {
            snapshots.maxByOrNull { it.snapshotDate }?.value ?: BigDecimal.ZERO
        } else {
            snapshots.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.value) }
        }
    }

    private fun computeCurrentValue(metricType: MetricType, snapshots: List<MetricsSnapshot>): BigDecimal {
        val aggregateSnapshots = snapshots.filter { it.dimensions.isEmpty() }
        return if (isPointInTimeMetric(metricType)) {
            aggregateSnapshots.maxByOrNull { it.snapshotDate }?.value ?: BigDecimal.ZERO
        } else {
            aggregateSnapshots.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.value) }
        }
    }

    private fun computeTrend(
        metricType: MetricType,
        from: LocalDate,
        to: LocalDate,
        locationId: UUID?,
    ): TrendComparison? {
        val periodLength = ChronoUnit.DAYS.between(from, to) + 1
        val previousFrom = from.minusDays(periodLength)
        val previousTo = from.minusDays(1)

        val currentSnapshots = fetchSnapshots(metricType, from, to, locationId)
        val previousSnapshots = fetchSnapshots(metricType, previousFrom, previousTo, locationId)

        val currentTotal = computeCurrentValue(metricType, currentSnapshots)
        val previousTotal = computeCurrentValue(metricType, previousSnapshots)

        val changeAbsolute = currentTotal.subtract(previousTotal)
        val changePercent = if (previousTotal.compareTo(BigDecimal.ZERO) != 0) {
            changeAbsolute.divide(previousTotal, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100))
        } else {
            null
        }

        return TrendComparison(
            currentPeriodTotal = currentTotal,
            previousPeriodTotal = previousTotal,
            changeAbsolute = changeAbsolute,
            changePercent = changePercent,
        )
    }

    private fun isPointInTimeMetric(metricType: MetricType): Boolean {
        return metricType in listOf(
            MetricType.ACTIVE_SUBSCRIPTIONS,
            MetricType.CLASS_UTILIZATION,
            MetricType.CHURN_RATE,
        )
    }

    private fun validateDateRange(from: LocalDate, to: LocalDate) {
        if (from.isAfter(to)) {
            throw InvalidMetricsQueryException("'from' date must not be after 'to' date")
        }
    }
}
