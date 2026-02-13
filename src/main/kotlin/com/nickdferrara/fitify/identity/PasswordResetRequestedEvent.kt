package com.nickdferrara.fitify.identity

import java.util.UUID

data class PasswordResetRequestedEvent(
    val userId: UUID,
    val email: String,
    val resetToken: String,
    val expiresInMinutes: Long,
)
