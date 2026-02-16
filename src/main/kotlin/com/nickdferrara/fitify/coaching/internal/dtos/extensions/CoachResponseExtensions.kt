package com.nickdferrara.fitify.coaching.internal.dtos.extensions

import com.nickdferrara.fitify.coaching.internal.dtos.response.CoachResponse
import com.nickdferrara.fitify.coaching.internal.entities.Coach

internal fun Coach.toResponse() = CoachResponse(
    id = id!!,
    name = name,
    bio = bio,
    photoUrl = photoUrl,
    specializations = specializations,
    active = active,
    createdAt = createdAt!!,
    certifications = certifications.map { it.toResponse() },
    locationIds = locations.map { it.locationId },
)
