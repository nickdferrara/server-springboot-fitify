package com.nickdferrara.fitify.coaching.internal.dtos.request

internal data class CreateCoachRequest(
    val name: String,
    val bio: String,
    val photoUrl: String? = null,
    val specializations: List<String> = emptyList(),
    val certifications: List<CertificationRequest> = emptyList(),
)
