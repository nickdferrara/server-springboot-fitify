package com.nickdferrara.fitify.coaching.internal.dtos.response

import com.nickdferrara.fitify.coaching.internal.entities.CoachCertification
import java.time.LocalDate
import java.util.UUID

internal data class CertificationResponse(
    val id: UUID,
    val name: String,
    val issuer: String,
    val validUntil: LocalDate?,
)

internal fun CoachCertification.toResponse() = CertificationResponse(
    id = id!!,
    name = name,
    issuer = issuer,
    validUntil = validUntil,
)
