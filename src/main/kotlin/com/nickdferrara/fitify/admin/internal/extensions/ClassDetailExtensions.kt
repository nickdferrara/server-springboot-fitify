package com.nickdferrara.fitify.admin.internal.extensions

import com.nickdferrara.fitify.admin.internal.dtos.response.AdminClassResponse
import com.nickdferrara.fitify.scheduling.ClassDetail

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
