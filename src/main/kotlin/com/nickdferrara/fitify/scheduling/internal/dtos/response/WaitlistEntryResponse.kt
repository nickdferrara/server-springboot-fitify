package com.nickdferrara.fitify.scheduling.internal.dtos.response

import com.nickdferrara.fitify.scheduling.internal.entities.WaitlistEntry
import java.time.Instant
import java.util.UUID

internal data class WaitlistEntryResponse(
    val id: UUID,
    val userId: UUID,
    val classId: UUID,
    val className: String,
    val startTime: Instant,
    val position: Int,
    val createdAt: Instant,
)

internal fun WaitlistEntry.toResponse() = WaitlistEntryResponse(
    id = id!!,
    userId = userId,
    classId = fitnessClass.id!!,
    className = fitnessClass.name,
    startTime = fitnessClass.startTime,
    position = position,
    createdAt = createdAt!!,
)
