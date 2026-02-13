package com.nickdferrara.fitify.location.internal.dtos.request

internal data class CreateLocationRequest(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String,
    val email: String,
    val timeZone: String,
    val operatingHours: List<OperatingHoursRequest> = emptyList(),
)
