package com.nickdferrara.fitify.location.internal.dtos.request

internal data class UpdateLocationRequest(
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val timeZone: String? = null,
    val operatingHours: List<OperatingHoursRequest>? = null,
)
