package com.nickdferrara.fitify.scheduling.internal.dtos.response

import com.nickdferrara.fitify.scheduling.internal.entities.BookingStatus
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import java.time.Instant
import java.util.UUID

internal data class ClassResponse(
    val id: UUID,
    val locationId: UUID,
    val name: String,
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

internal fun FitnessClass.toResponse() = ClassResponse(
    id = id!!,
    locationId = locationId,
    name = name,
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
