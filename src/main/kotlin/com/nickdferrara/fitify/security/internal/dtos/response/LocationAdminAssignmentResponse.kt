package com.nickdferrara.fitify.security.internal.dtos.response

import java.time.Instant
import java.util.UUID

internal data class LocationAdminAssignmentResponse(
    val id: UUID,
    val keycloakId: String,
    val locationId: UUID,
    val createdAt: Instant,
)
