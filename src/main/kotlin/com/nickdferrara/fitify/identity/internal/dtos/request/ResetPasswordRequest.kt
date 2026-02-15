package com.nickdferrara.fitify.identity.internal.dtos.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

internal data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,
    @field:NotBlank
    @field:Size(min = 8)
    val newPassword: String,
)
