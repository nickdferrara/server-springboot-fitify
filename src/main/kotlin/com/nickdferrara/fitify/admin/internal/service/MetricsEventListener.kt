package com.nickdferrara.fitify.admin.internal.service

import com.nickdferrara.fitify.admin.internal.entities.MetricType
import com.nickdferrara.fitify.admin.internal.entities.MetricsSnapshot
import com.nickdferrara.fitify.admin.internal.repository.MetricsSnapshotRepository
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.shared.Result
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Component
internal class MetricsEventListener(
    private val metricsSnapshotRepository: MetricsSnapshotRepository,
    private val schedulingApi: SchedulingApi,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Transactional
    fun onUserRegistered(event: UserRegisteredEvent) {
        log.debug("Metrics: recording signup for user {}", event.userId)
        incrementMetric(MetricType.SIGNUPS, locationId = null, dimensions = emptyMap())
    }

    @EventListener
    @Transactional
    fun onBookingCancelled(event: BookingCancelledEvent) {
        log.debug("Metrics: recording cancellation for class {}", event.classId)
        when (val classResult = schedulingApi.findClassById(event.classId)) {
            is Result.Success -> {
                val classSummary = classResult.value
                incrementMetric(
                    MetricType.CANCELLATIONS,
                    locationId = classSummary.locationId,
                    dimensions = mapOf("class_type" to classSummary.classType),
                )
            }
            is Result.Failure -> {
                log.warn("Metrics: could not find class {} for cancellation metric", event.classId)
                incrementMetric(MetricType.CANCELLATIONS, locationId = null, dimensions = emptyMap())
            }
        }
    }

    @EventListener
    @Transactional
    fun onSubscriptionCreated(event: SubscriptionCreatedEvent) {
        log.debug("Metrics: recording revenue for subscription {}", event.subscriptionId)
        incrementMetric(
            MetricType.REVENUE,
            locationId = null,
            dimensions = mapOf("plan_type" to event.planType),
        )
    }

    @EventListener
    @Transactional
    fun onWaitlistPromoted(event: WaitlistPromotedEvent) {
        log.debug("Metrics: recording waitlist promotion for class {}", event.classId)
        when (val classResult = schedulingApi.findClassById(event.classId)) {
            is Result.Success -> {
                val classSummary = classResult.value
                incrementMetric(
                    MetricType.WAITLIST_CONVERSION,
                    locationId = classSummary.locationId,
                    dimensions = mapOf("class_type" to classSummary.classType),
                )
            }
            is Result.Failure -> {
                log.warn("Metrics: could not find class {} for waitlist conversion metric", event.classId)
                incrementMetric(MetricType.WAITLIST_CONVERSION, locationId = null, dimensions = emptyMap())
            }
        }
    }

    private fun incrementMetric(
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
