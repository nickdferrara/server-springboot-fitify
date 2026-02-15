package com.nickdferrara.fitify.security.internal.dtos.request

import jakarta.validation.constraints.NotBlank
import java.util.UUID

internal data class AssignLocationAdminRequest(
    @field:NotBlank
    val keycloakId: String,
    val locationId: UUID,
)
