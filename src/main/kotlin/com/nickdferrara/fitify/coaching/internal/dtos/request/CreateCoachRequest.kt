package com.nickdferrara.fitify.coaching.internal.dtos.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

internal data class CreateCoachRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val bio: String,
    val photoUrl: String? = null,
    val specializations: List<String> = emptyList(),
    @field:Valid
    val certifications: List<CertificationRequest> = emptyList(),
)
