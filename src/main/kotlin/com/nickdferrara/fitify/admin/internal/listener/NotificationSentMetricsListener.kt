package com.nickdferrara.fitify.admin.internal.listener

import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.notification.NotificationSentEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class NotificationSentMetricsListener(
    private val metricsRecorder: MetricsRecorder,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Transactional
    fun onNotificationSent(event: NotificationSentEvent) {
        log.debug("Metrics: recording notification {} via {}", event.notificationId, event.channel)
        metricsRecorder.incrementMetric(
            MetricType.NOTIFICATIONS_SENT,
            locationId = null,
            dimensions = mapOf("channel" to event.channel, "status" to event.status),
        )
    }
}
