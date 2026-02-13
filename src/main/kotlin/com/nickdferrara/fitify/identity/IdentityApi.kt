package com.nickdferrara.fitify.identity

import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.Result
import java.util.UUID

data class IdentityUserSummary(
    val id: UUID,
    val email: String,
    val displayName: String,
)

interface IdentityApi {
    fun findUserById(id: UUID): Result<IdentityUserSummary, DomainError>
    fun findUserByEmail(email: String): Result<IdentityUserSummary, DomainError>
}
