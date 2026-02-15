package com.nickdferrara.fitify.notification.internal.dtos.request

import jakarta.validation.constraints.NotBlank

internal data class RegisterDeviceTokenRequest(
    @field:NotBlank
    val token: String,
    @field:NotBlank
    val deviceType: String,
)
