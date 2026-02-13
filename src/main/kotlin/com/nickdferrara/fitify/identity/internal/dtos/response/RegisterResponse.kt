package com.nickdferrara.fitify.identity.internal.dtos.response

import com.nickdferrara.fitify.identity.internal.entities.User
import java.util.UUID

internal data class RegisterResponse(
    val id: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
)

internal fun User.toRegisterResponse() = RegisterResponse(
    id = id!!,
    email = email,
    firstName = firstName,
    lastName = lastName,
)
