package com.nickdferrara.fitify.identity

import java.time.Instant
import java.util.UUID

data class PasswordResetRequestedEvent(
    val userId: UUID,
    val email: String,
    val resetLink: String,
    val expiresAt: Instant,
)
