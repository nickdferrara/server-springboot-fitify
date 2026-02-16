package com.nickdferrara.fitify.location.internal.dtos.response

import java.time.Instant
import java.util.UUID

internal data class LocationResponse(
    val id: UUID,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String,
    val email: String,
    val timeZone: String,
    val active: Boolean,
    val createdAt: Instant,
    val operatingHours: List<OperatingHoursResponse>,
)
