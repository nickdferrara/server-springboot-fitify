package com.nickdferrara.fitify.coaching.internal.listener

import com.nickdferrara.fitify.location.LocationUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class LocationUpdatedEventListener {

    private val logger = LoggerFactory.getLogger(LocationUpdatedEventListener::class.java)

    @TransactionalEventListener
    fun onLocationUpdated(event: LocationUpdatedEvent) {
        logger.info("Location {} updated fields: {}, reviewing coach assignments", event.locationId, event.updatedFields)
    }
}
