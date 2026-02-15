package com.nickdferrara.fitify.location.internal.dtos.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

internal data class UpdateLocationRequest(
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    @field:Size(min = 2, max = 2)
    val state: String? = null,
    val zipCode: String? = null,
    val phone: String? = null,
    @field:Email
    val email: String? = null,
    val timeZone: String? = null,
    @field:Valid
    val operatingHours: List<OperatingHoursRequest>? = null,
)
