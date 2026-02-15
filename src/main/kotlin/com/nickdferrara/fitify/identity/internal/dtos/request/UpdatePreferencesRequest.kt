package com.nickdferrara.fitify.identity.internal.dtos.request

import jakarta.validation.constraints.NotBlank

internal data class UpdatePreferencesRequest(
    @field:NotBlank
    val theme: String,
)
