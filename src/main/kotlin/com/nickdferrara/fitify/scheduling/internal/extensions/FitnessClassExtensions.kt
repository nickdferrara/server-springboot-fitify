package com.nickdferrara.fitify.scheduling.internal.extensions

import com.nickdferrara.fitify.scheduling.ClassDetail
import com.nickdferrara.fitify.scheduling.ClassSummary
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ClassResponse
import com.nickdferrara.fitify.scheduling.internal.enums.BookingStatus
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass

internal fun FitnessClass.toSummary() = ClassSummary(
    id = id!!,
    name = name,
    description = description,
    classType = classType,
    startTime = startTime,
    endTime = endTime,
    locationId = locationId,
    coachId = coachId,
)

internal fun FitnessClass.toDetail() = ClassDetail(
    id = id!!,
    name = name,
    description = description,
    classType = classType,
    coachId = coachId,
    room = room,
    startTime = startTime,
    endTime = endTime,
    capacity = capacity,
    locationId = locationId,
    status = status.name,
    enrolledCount = bookings.count { it.status == BookingStatus.CONFIRMED },
    waitlistSize = waitlistEntries.size,
    createdAt = createdAt!!,
)

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
