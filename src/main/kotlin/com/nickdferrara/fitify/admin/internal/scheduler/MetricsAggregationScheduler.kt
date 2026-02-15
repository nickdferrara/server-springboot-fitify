package com.nickdferrara.fitify.admin.internal.scheduler

import com.nickdferrara.fitify.admin.internal.entities.MetricType
import com.nickdferrara.fitify.admin.internal.entities.MetricsSnapshot
import com.nickdferrara.fitify.admin.internal.repository.MetricsSnapshotRepository
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.subscription.SubscriptionApi
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@Component
internal class MetricsAggregationScheduler(
    private val metricsSnapshotRepository: MetricsSnapshotRepository,
    private val subscriptionApi: SubscriptionApi,
    private val schedulingApi: SchedulingApi,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${fitify.metrics.aggregation-cron:0 0 2 * * *}")
    @Transactional
    fun runDailyAggregation() {
        log.info("Starting daily metrics aggregation")
        val yesterday = LocalDate.now().minusDays(1)
        aggregateActiveSubscriptions(yesterday)
        aggregateRevenue(yesterday)
        aggregateClassUtilization(yesterday)
        aggregateChurnRate(yesterday)
        log.info("Daily metrics aggregation completed")
    }

    internal fun aggregateActiveSubscriptions(date: LocalDate) {
        val countsByPlanType = subscriptionApi.countActiveSubscriptionsByPlanType()
        var total = 0L

        for ((planType, count) in countsByPlanType) {
            upsertSnapshot(
                metricType = MetricType.ACTIVE_SUBSCRIPTIONS,
                date = date,
                locationId = null,
                dimensions = mapOf("plan_type" to planType),
                value = BigDecimal.valueOf(count),
            )
            total += count
        }

        upsertSnapshot(
            metricType = MetricType.ACTIVE_SUBSCRIPTIONS,
            date = date,
            locationId = null,
            dimensions = emptyMap(),
            value = BigDecimal.valueOf(total),
        )
    }

    internal fun aggregateRevenue(date: LocalDate) {
        val dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val dayEnd = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val revenueByPlanType = subscriptionApi.sumRevenueBetweenByPlanType(dayStart, dayEnd)
        var total = BigDecimal.ZERO

        for ((planType, revenue) in revenueByPlanType) {
            upsertSnapshot(
                metricType = MetricType.REVENUE,
                date = date,
                locationId = null,
                dimensions = mapOf("plan_type" to planType),
                value = revenue,
            )
            total = total.add(revenue)
        }

        upsertSnapshot(
            metricType = MetricType.REVENUE,
            date = date,
            locationId = null,
            dimensions = emptyMap(),
            value = total,
        )
    }

    internal fun aggregateClassUtilization(date: LocalDate) {
        val dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val dayEnd = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val utilizations = schedulingApi.getClassUtilizationByDateRange(dayStart, dayEnd)
        val byLocation = utilizations.groupBy { it.locationId }

        for ((locationId, classes) in byLocation) {
            val avgUtilization = if (classes.isEmpty()) {
                BigDecimal.ZERO
            } else {
                val totalUtilization = classes.sumOf { cls ->
                    if (cls.capacity > 0) {
                        cls.enrolledCount.toBigDecimal()
                            .divide(cls.capacity.toBigDecimal(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal(100))
                    } else {
                        BigDecimal.ZERO
                    }
                }
                totalUtilization.divide(classes.size.toBigDecimal(), 2, RoundingMode.HALF_UP)
            }

            upsertSnapshot(
                metricType = MetricType.CLASS_UTILIZATION,
                date = date,
                locationId = locationId,
                dimensions = emptyMap(),
                value = avgUtilization,
            )
        }
    }

    internal fun aggregateChurnRate(date: LocalDate) {
        val monthStart = date.withDayOfMonth(1)
        if (date != monthStart) return

        val previousMonthStart = monthStart.minusMonths(1)

        val startInstant = previousMonthStart.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endInstant = monthStart.atStartOfDay().toInstant(ZoneOffset.UTC)

        val expiredCount = subscriptionApi.countSubscriptionsExpiredBetween(startInstant, endInstant)
        val activeAtStart = subscriptionApi.countActiveSubscriptionsAsOf(startInstant)

        val churnRate = if (activeAtStart > 0) {
            BigDecimal.valueOf(expiredCount)
                .divide(BigDecimal.valueOf(activeAtStart), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
        } else {
            BigDecimal.ZERO
        }

        val previousMonthEnd = monthStart.minusDays(1)
        upsertSnapshot(
            metricType = MetricType.CHURN_RATE,
            date = previousMonthEnd,
            locationId = null,
            dimensions = emptyMap(),
            value = churnRate,
        )
    }

    private fun upsertSnapshot(
        metricType: MetricType,
        date: LocalDate,
        locationId: UUID?,
        dimensions: Map<String, String>,
        value: BigDecimal,
    ) {
        val existing = findSnapshot(metricType, date, locationId, dimensions)
        if (existing != null) {
            existing.value = value
            metricsSnapshotRepository.save(existing)
        } else {
            metricsSnapshotRepository.save(
                MetricsSnapshot(
                    metricType = metricType,
                    locationId = locationId,
                    dimensions = dimensions,
                    value = value,
                    snapshotDate = date,
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
