package com.nickdferrara.fitify.admin.internal.dtos.request

import java.time.Instant
import java.util.UUID

internal data class UpdateClassRequest(
    val name: String? = null,
    val description: String? = null,
    val classType: String? = null,
    val coachId: UUID? = null,
    val room: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val capacity: Int? = null,
)
