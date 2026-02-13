package com.nickdferrara.fitify.location

import java.time.Instant
import java.util.UUID

data class LocationDeactivatedEvent(
    val locationId: UUID,
    val effectiveDate: Instant,
)
