package com.nickdferrara.fitify.location.internal.extensions

import com.nickdferrara.fitify.location.internal.dtos.response.LocationResponse
import com.nickdferrara.fitify.location.internal.dtos.response.OperatingHoursResponse
import com.nickdferrara.fitify.location.internal.entities.Location

internal fun Location.toResponse() = LocationResponse(
    id = id!!,
    name = name,
    address = address,
    city = city,
    state = state,
    zipCode = zipCode,
    phone = phone,
    email = email,
    timeZone = timeZone,
    active = active,
    createdAt = createdAt!!,
    operatingHours = operatingHours.map { it.toResponse() },
)
