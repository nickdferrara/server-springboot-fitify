package com.nickdferrara.fitify.identity.internal.dtos.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

internal data class ForgotPasswordRequest(
    @field:NotBlank
    @field:Email
    val email: String,
)
