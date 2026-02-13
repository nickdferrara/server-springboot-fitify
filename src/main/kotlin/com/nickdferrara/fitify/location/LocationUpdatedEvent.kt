package com.nickdferrara.fitify.location

import java.util.UUID

data class LocationUpdatedEvent(
    val locationId: UUID,
    val updatedFields: Set<String>,
)
