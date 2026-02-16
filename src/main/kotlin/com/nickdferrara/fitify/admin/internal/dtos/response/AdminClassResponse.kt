package com.nickdferrara.fitify.admin.internal.dtos.response

import java.time.Instant
import java.util.UUID

internal data class AdminClassResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val classType: String,
    val coachId: UUID,
    val room: String?,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int,
    val locationId: UUID,
    val status: String,
    val enrolledCount: Int,
    val waitlistSize: Int,
    val createdAt: Instant,
)
