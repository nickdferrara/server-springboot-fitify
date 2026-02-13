package com.nickdferrara.fitify.coaching.internal.dtos.request

import java.util.UUID

internal data class AssignCoachLocationsRequest(
    val locationIds: List<UUID>,
)
