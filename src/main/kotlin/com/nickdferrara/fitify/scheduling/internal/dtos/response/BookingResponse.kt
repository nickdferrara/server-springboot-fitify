package com.nickdferrara.fitify.scheduling.internal.dtos.response

import java.time.Instant
import java.util.UUID

internal data class BookingResponse(
    val id: UUID,
    val userId: UUID,
    val classId: UUID,
    val className: String,
    val startTime: Instant,
    val status: String,
    val bookedAt: Instant,
)
