package com.nickdferrara.fitify.location.internal.extensions

import com.nickdferrara.fitify.location.internal.dtos.response.OperatingHoursResponse
import com.nickdferrara.fitify.location.internal.entities.LocationOperatingHours

internal fun LocationOperatingHours.toResponse() = OperatingHoursResponse(
    id = id!!,
    dayOfWeek = dayOfWeek,
    openTime = openTime,
    closeTime = closeTime,
)
