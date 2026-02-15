package com.nickdferrara.fitify.scheduling.internal.listener

import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class LocationEventListenerTest {

    private val listener = LocationEventListener()

    @Test
    fun `onLocationCreated handles event without error`() {
        val event = LocationCreatedEvent(
            locationId = UUID.randomUUID(),
            name = "Downtown Gym",
            address = "123 Main St",
            timeZone = "America/New_York",
        )

        listener.onLocationCreated(event)
    }

    @Test
    fun `onLocationUpdated handles event without error`() {
        val event = LocationUpdatedEvent(
            locationId = UUID.randomUUID(),
            updatedFields = setOf("name", "address"),
        )

        listener.onLocationUpdated(event)
    }

    @Test
    fun `onLocationDeactivated handles event without error`() {
        val event = LocationDeactivatedEvent(
            locationId = UUID.randomUUID(),
            effectiveDate = Instant.now(),
        )

        listener.onLocationDeactivated(event)
    }
}
