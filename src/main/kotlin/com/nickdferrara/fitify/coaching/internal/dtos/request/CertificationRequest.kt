package com.nickdferrara.fitify.coaching.internal.dtos.request

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

internal data class CertificationRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val issuer: String,
    val validUntil: LocalDate? = null,
)
