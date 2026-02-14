package com.nickdferrara.fitify.admin.internal.dtos.request

import java.time.Instant
import java.util.UUID

internal data class CreateClassRequest(
    val name: String,
    val description: String? = null,
    val classType: String,
    val coachId: UUID,
    val room: String? = null,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int,
)
