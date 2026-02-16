package com.nickdferrara.fitify.coaching.internal.dtos.extensions

import com.nickdferrara.fitify.coaching.internal.dtos.response.CertificationResponse
import com.nickdferrara.fitify.coaching.internal.entities.CoachCertification

internal fun CoachCertification.toResponse() = CertificationResponse(
    id = id!!,
    name = name,
    issuer = issuer,
    validUntil = validUntil,
)
