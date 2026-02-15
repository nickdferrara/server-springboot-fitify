package com.nickdferrara.fitify.scheduling.internal.dtos.response

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
