package com.nickdferrara.fitify.scheduling.internal.extensions

import com.nickdferrara.fitify.scheduling.internal.dtos.response.ClassResponse
import com.nickdferrara.fitify.scheduling.internal.enums.BookingStatus
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass

internal fun FitnessClass.toResponse() = ClassResponse(
    id = id!!,
    locationId = locationId,
    name = name,
    description = description,
    classType = classType,
    coachId = coachId,
    room = room,
    startTime = startTime,
    endTime = endTime,
    capacity = capacity,
    status = status.name,
    enrolledCount = bookings.count { it.status == BookingStatus.CONFIRMED },
    waitlistSize = waitlistEntries.size,
    createdAt = createdAt!!,
)
