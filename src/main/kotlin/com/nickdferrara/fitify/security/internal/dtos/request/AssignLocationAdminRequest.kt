package com.nickdferrara.fitify.security.internal.dtos.request

import java.util.UUID

internal data class AssignLocationAdminRequest(
    val keycloakId: String,
    val locationId: UUID,
)
