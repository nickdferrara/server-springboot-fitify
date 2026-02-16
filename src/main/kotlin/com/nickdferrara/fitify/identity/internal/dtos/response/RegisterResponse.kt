package com.nickdferrara.fitify.identity.internal.dtos.response

import java.util.UUID

internal data class RegisterResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
)
