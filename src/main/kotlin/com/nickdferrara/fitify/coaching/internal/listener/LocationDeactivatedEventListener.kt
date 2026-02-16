package com.nickdferrara.fitify.coaching.internal.listener

import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class LocationDeactivatedEventListener {

    private val logger = LoggerFactory.getLogger(LocationDeactivatedEventListener::class.java)

    @TransactionalEventListener
    fun onLocationDeactivated(event: LocationDeactivatedEvent) {
        logger.info(
            "Location {} deactivated effective {}, removing from active coach assignments",
            event.locationId, event.effectiveDate,
        )
    }
}
