package com.nickdferrara.fitify.scheduling.internal.dtos.response

import java.time.Instant
import java.util.UUID

internal data class ClassResponse(
    val id: UUID,
    val locationId: UUID,
    val name: String,
    val description: String?,
    val classType: String,
    val coachId: UUID,
    val room: String?,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int,
    val status: String,
    val enrolledCount: Int,
    val waitlistSize: Int,
    val createdAt: Instant,
)
