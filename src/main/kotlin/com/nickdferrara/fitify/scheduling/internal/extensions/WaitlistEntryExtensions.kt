package com.nickdferrara.fitify.scheduling.internal.extensions

import com.nickdferrara.fitify.scheduling.internal.dtos.response.WaitlistEntryResponse
import com.nickdferrara.fitify.scheduling.internal.entities.WaitlistEntry

internal fun WaitlistEntry.toResponse() = WaitlistEntryResponse(
    id = id!!,
    userId = userId,
    classId = fitnessClass.id!!,
    className = fitnessClass.name,
    startTime = fitnessClass.startTime,
    position = position,
    createdAt = createdAt!!,
)
