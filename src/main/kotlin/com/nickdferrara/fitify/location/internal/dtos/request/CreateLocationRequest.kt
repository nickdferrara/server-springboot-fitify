package com.nickdferrara.fitify.location.internal.dtos.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

internal data class CreateLocationRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val address: String,
    @field:NotBlank
    val city: String,
    @field:NotBlank
    @field:Size(min = 2, max = 2)
    val state: String,
    @field:NotBlank
    val zipCode: String,
    @field:NotBlank
    val phone: String,
    @field:NotBlank
    @field:Email
    val email: String,
    @field:NotBlank
    val timeZone: String,
    @field:NotEmpty
    @field:Valid
    val operatingHours: List<OperatingHoursRequest> = emptyList(),
)
