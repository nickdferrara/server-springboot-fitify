package com.nickdferrara.fitify.coaching.internal.dtos.response

import com.nickdferrara.fitify.coaching.internal.entities.Coach
import java.time.Instant
import java.util.UUID

internal data class CoachResponse(
    val id: UUID,
    val name: String,
    val bio: String,
    val photoUrl: String?,
    val specializations: List<String>,
    val active: Boolean,
    val createdAt: Instant,
    val certifications: List<CertificationResponse>,
    val locationIds: List<UUID>,
)

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
