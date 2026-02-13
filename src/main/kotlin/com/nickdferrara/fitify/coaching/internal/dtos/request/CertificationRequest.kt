package com.nickdferrara.fitify.coaching.internal.dtos.request

import java.time.LocalDate

internal data class CertificationRequest(
    val name: String,
    val issuer: String,
    val validUntil: LocalDate? = null,
)
