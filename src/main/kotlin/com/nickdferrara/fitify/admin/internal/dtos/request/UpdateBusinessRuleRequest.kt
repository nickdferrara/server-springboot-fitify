package com.nickdferrara.fitify.admin.internal.dtos.request

import jakarta.validation.constraints.NotBlank
import java.util.UUID

internal data class UpdateBusinessRuleRequest(
    @field:NotBlank
    val value: String,
    val description: String? = null,
    val locationId: UUID? = null,
)
