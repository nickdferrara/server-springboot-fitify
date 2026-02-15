package com.nickdferrara.fitify.coaching.internal.dtos.request

import jakarta.validation.Valid

internal data class UpdateCoachRequest(
    val name: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val specializations: List<String>? = null,
    @field:Valid
    val certifications: List<CertificationRequest>? = null,
)
