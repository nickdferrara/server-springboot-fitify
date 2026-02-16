package com.nickdferrara.fitify.admin.internal.listener

import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.location.LocationCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class LocationCreatedMetricsListener(
    private val metricsRecorder: MetricsRecorder,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Transactional
    fun onLocationCreated(event: LocationCreatedEvent) {
        log.debug("Metrics: recording new location {}", event.locationId)
        metricsRecorder.incrementMetric(
            MetricType.LOCATIONS,
            locationId = event.locationId,
            dimensions = mapOf("time_zone" to event.timeZone),
        )
    }
}
