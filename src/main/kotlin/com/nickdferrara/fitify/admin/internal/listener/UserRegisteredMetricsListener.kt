package com.nickdferrara.fitify.admin.internal.listener

import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class UserRegisteredMetricsListener(
    private val metricsRecorder: MetricsRecorder,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Transactional
    fun onUserRegistered(event: UserRegisteredEvent) {
        log.debug("Metrics: recording signup for user {}", event.userId)
        metricsRecorder.incrementMetric(MetricType.SIGNUPS, locationId = null, dimensions = emptyMap())
    }
}
