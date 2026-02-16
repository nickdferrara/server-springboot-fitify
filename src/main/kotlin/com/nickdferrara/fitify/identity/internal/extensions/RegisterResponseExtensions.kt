package com.nickdferrara.fitify.identity.internal.extensions

import com.nickdferrara.fitify.identity.internal.dtos.response.RegisterResponse
import com.nickdferrara.fitify.identity.internal.entities.User

internal fun User.toRegisterResponse() = RegisterResponse(
    id = id!!,
    email = email,
    firstName = firstName,
    lastName = lastName,
)
