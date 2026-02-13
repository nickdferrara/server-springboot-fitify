package com.nickdferrara.fitify.coaching.internal.dtos.request

internal data class UpdateCoachRequest(
    val name: String? = null,
    val bio: String? = null,
    val photoUrl: String? = null,
    val specializations: List<String>? = null,
    val certifications: List<CertificationRequest>? = null,
)
