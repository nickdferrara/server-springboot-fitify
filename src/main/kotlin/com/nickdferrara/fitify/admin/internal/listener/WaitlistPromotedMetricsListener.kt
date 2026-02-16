package com.nickdferrara.fitify.admin.internal.listener

import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.shared.Result
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
internal class WaitlistPromotedMetricsListener(
    private val metricsRecorder: MetricsRecorder,
    private val schedulingApi: SchedulingApi,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Transactional
    fun onWaitlistPromoted(event: WaitlistPromotedEvent) {
        log.debug("Metrics: recording waitlist promotion for class {}", event.classId)
        when (val classResult = schedulingApi.findClassById(event.classId)) {
            is Result.Success -> {
                val classSummary = classResult.value
                metricsRecorder.incrementMetric(
                    MetricType.WAITLIST_CONVERSION,
                    locationId = classSummary.locationId,
                    dimensions = mapOf("class_type" to classSummary.classType),
                )
            }
            is Result.Failure -> {
                log.warn("Metrics: could not find class {} for waitlist conversion metric", event.classId)
                metricsRecorder.incrementMetric(MetricType.WAITLIST_CONVERSION, locationId = null, dimensions = emptyMap())
            }
        }
    }
}
