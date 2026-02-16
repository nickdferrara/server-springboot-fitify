package com.nickdferrara.fitify.admin.internal.listener

import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class SubscriptionCreatedMetricsListener(
    private val metricsRecorder: MetricsRecorder,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Transactional
    fun onSubscriptionCreated(event: SubscriptionCreatedEvent) {
        log.debug("Metrics: recording revenue for subscription {}", event.subscriptionId)
        metricsRecorder.incrementMetric(
            MetricType.REVENUE,
            locationId = null,
            dimensions = mapOf("plan_type" to event.planType),
        )
    }
}
