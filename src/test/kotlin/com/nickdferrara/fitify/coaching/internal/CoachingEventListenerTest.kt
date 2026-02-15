package com.nickdferrara.fitify.coaching.internal

import com.nickdferrara.fitify.coaching.internal.service.CoachingEventListener
import com.nickdferrara.fitify.coaching.internal.service.CoachingService
import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CoachingEventListenerTest {

    private val coachingService = mockk<CoachingService>(relaxed = true)
    private val listener = CoachingEventListener(coachingService)

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
