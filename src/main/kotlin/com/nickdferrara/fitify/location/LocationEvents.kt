package com.nickdferrara.fitify.location

import java.time.Instant
import java.util.UUID

data class LocationCreatedEvent(
    val locationId: UUID,
    val name: String,
    val address: String,
    val timeZone: String,
)

data class LocationUpdatedEvent(
    val locationId: UUID,
    val updatedFields: Set<String>,
)

data class LocationDeactivatedEvent(
    val locationId: UUID,
    val effectiveDate: Instant,
)
