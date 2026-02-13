package com.nickdferrara.fitify.location

import java.util.UUID

data class LocationCreatedEvent(
    val locationId: UUID,
    val name: String,
    val address: String,
    val timeZone: String,
)
