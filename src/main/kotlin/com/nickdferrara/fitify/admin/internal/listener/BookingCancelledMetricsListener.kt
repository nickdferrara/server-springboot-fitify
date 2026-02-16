package com.nickdferrara.fitify.admin.internal.listener

import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.shared.Result
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class BookingCancelledMetricsListener(
    private val metricsRecorder: MetricsRecorder,
    private val schedulingApi: SchedulingApi,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Transactional
    fun onBookingCancelled(event: BookingCancelledEvent) {
        log.debug("Metrics: recording cancellation for class {}", event.classId)
        when (val classResult = schedulingApi.findClassById(event.classId)) {
            is Result.Success -> {
                val classSummary = classResult.value
                metricsRecorder.incrementMetric(
                    MetricType.CANCELLATIONS,
                    locationId = classSummary.locationId,
                    dimensions = mapOf("class_type" to classSummary.classType),
                )
            }
            is Result.Failure -> {
                log.warn("Metrics: could not find class {} for cancellation metric", event.classId)
                metricsRecorder.incrementMetric(MetricType.CANCELLATIONS, locationId = null, dimensions = emptyMap())
            }
        }
    }
}
