package com.nickdferrara.fitify.identity.internal.repository

import com.nickdferrara.fitify.identity.internal.entities.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

internal interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<PasswordResetToken>
}
