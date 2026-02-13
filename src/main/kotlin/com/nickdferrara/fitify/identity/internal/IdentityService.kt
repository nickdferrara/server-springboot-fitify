package com.nickdferrara.fitify.identity.internal

import com.nickdferrara.fitify.identity.IdentityApi
import com.nickdferrara.fitify.identity.IdentityUserSummary
import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import org.springframework.stereotype.Service
import java.util.UUID

@Service
internal class IdentityService : IdentityApi {

    override fun findUserById(id: UUID): Result<IdentityUserSummary, DomainError> {
        return Result.Failure(NotFoundError("User not found: $id"))
    }

    override fun findUserByEmail(email: String): Result<IdentityUserSummary, DomainError> {
        return Result.Failure(NotFoundError("User not found: $email"))
    }
}
