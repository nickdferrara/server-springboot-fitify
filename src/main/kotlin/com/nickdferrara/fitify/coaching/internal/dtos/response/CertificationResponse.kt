package com.nickdferrara.fitify.coaching.internal.dtos.response

import java.time.LocalDate
import java.util.UUID

internal data class CertificationResponse(
    val id: UUID,
    val name: String,
    val issuer: String,
    val validUntil: LocalDate?,
)
