package com.nickdferrara.fitify.scheduling.internal.extensions

import com.nickdferrara.fitify.scheduling.internal.dtos.response.BookingResponse
import com.nickdferrara.fitify.scheduling.internal.entities.Booking

internal fun Booking.toResponse() = BookingResponse(
    id = id!!,
    userId = userId,
    classId = fitnessClass.id!!,
    className = fitnessClass.name,
    startTime = fitnessClass.startTime,
    status = status.name,
    bookedAt = bookedAt!!,
)
