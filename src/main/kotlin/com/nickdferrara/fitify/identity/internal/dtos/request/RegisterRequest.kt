package com.nickdferrara.fitify.identity.internal.dtos.request

internal data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
)
