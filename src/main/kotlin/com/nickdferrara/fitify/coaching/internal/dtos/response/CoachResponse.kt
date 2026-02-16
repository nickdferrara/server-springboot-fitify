package com.nickdferrara.fitify.coaching.internal.dtos.response

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
