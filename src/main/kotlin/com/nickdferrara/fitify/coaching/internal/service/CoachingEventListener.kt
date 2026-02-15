package com.nickdferrara.fitify.coaching.internal.service

import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class CoachingEventListener(
    private val coachingService: CoachingService,
) {

    private val logger = LoggerFactory.getLogger(CoachingEventListener::class.java)

    @TransactionalEventListener
    fun onLocationCreated(event: LocationCreatedEvent) {
        logger.info("New location {} available for coach assignments: {}", event.locationId, event.name)
    }

    @TransactionalEventListener
    fun onLocationUpdated(event: LocationUpdatedEvent) {
        logger.info("Location {} updated fields: {}, reviewing coach assignments", event.locationId, event.updatedFields)
    }

    @TransactionalEventListener
    fun onLocationDeactivated(event: LocationDeactivatedEvent) {
        logger.info(
            "Location {} deactivated effective {}, removing from active coach assignments",
            event.locationId, event.effectiveDate,
        )
    }
}
