package com.nickdferrara.fitify.coaching.internal.listener

import com.nickdferrara.fitify.location.LocationCreatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class LocationCreatedEventListener {

    private val logger = LoggerFactory.getLogger(LocationCreatedEventListener::class.java)

    @TransactionalEventListener
    fun onLocationCreated(event: LocationCreatedEvent) {
        logger.info("New location {} available for coach assignments: {}", event.locationId, event.name)
    }
}
