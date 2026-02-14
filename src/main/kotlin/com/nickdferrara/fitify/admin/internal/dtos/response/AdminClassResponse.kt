package com.nickdferrara.fitify.admin.internal.dtos.response

import com.nickdferrara.fitify.scheduling.ClassDetail
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

internal fun ClassDetail.toAdminResponse() = AdminClassResponse(
    id = id,
    name = name,
    description = description,
    classType = classType,
    coachId = coachId,
    room = room,
    startTime = startTime,
    endTime = endTime,
    capacity = capacity,
    locationId = locationId,
    status = status,
    enrolledCount = enrolledCount,
    waitlistSize = waitlistSize,
    createdAt = createdAt,
)
