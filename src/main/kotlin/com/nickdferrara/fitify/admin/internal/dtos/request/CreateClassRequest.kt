package com.nickdferrara.fitify.admin.internal.dtos.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.Instant
import java.util.UUID

internal data class CreateClassRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    @field:NotBlank
    val classType: String,
    val coachId: UUID,
    val room: String? = null,
    val startTime: Instant,
    val endTime: Instant,
    @field:Positive
    val capacity: Int,
)
