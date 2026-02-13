package com.nickdferrara.fitify.identity.internal.dtos.request

internal data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
)
