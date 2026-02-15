package com.nickdferrara.fitify.scheduling.internal.listener

import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class LocationEventListener {

    private val logger = LoggerFactory.getLogger(LocationEventListener::class.java)

    @TransactionalEventListener
    fun onLocationCreated(event: LocationCreatedEvent) {
        logger.info("New location {} available for scheduling: {}", event.locationId, event.name)
    }

    @TransactionalEventListener
    fun onLocationUpdated(event: LocationUpdatedEvent) {
        logger.info("Location {} updated fields: {}", event.locationId, event.updatedFields)
    }

    @TransactionalEventListener
    fun onLocationDeactivated(event: LocationDeactivatedEvent) {
        logger.info(
            "Location {} deactivated effective {}, flagging affected classes",
            event.locationId, event.effectiveDate,
        )
    }
}
