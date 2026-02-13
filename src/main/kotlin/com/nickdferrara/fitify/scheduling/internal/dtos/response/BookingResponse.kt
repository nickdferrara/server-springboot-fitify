package com.nickdferrara.fitify.scheduling.internal.dtos.response

import com.nickdferrara.fitify.scheduling.internal.entities.Booking
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

internal fun Booking.toResponse() = BookingResponse(
    id = id!!,
    userId = userId,
    classId = fitnessClass.id!!,
    className = fitnessClass.name,
    startTime = fitnessClass.startTime,
    status = status.name,
    bookedAt = bookedAt!!,
)
