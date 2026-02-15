package com.nickdferrara.fitify.coaching.internal.dtos.request

import jakarta.validation.constraints.NotEmpty
import java.util.UUID

internal data class AssignCoachLocationsRequest(
    @field:NotEmpty
    val locationIds: List<UUID>,
)
