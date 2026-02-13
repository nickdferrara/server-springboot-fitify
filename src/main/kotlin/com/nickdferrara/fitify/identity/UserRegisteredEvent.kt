package com.nickdferrara.fitify.identity

import java.util.UUID

data class UserRegisteredEvent(
    val userId: UUID,
    val email: String,
    val firstName: String,
    val lastName: String,
)
