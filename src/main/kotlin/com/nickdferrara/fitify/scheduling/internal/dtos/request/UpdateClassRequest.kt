package com.nickdferrara.fitify.scheduling.internal.dtos.request

import jakarta.validation.constraints.Positive
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
    @field:Positive
    val capacity: Int? = null,
)
