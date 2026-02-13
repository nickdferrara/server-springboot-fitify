package com.nickdferrara.fitify.scheduling.internal.dtos.request

import java.time.Instant
import java.util.UUID

internal data class CreateClassRequest(
    val name: String,
    val classType: String,
    val coachId: UUID,
    val room: String? = null,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int,
)
