package com.nickdferrara.fitify.identity.internal

import com.nickdferrara.fitify.identity.IdentityApi
import com.nickdferrara.fitify.identity.IdentityUserSummary
import com.nickdferrara.fitify.identity.internal.dtos.request.UpdatePreferencesRequest
import com.nickdferrara.fitify.identity.internal.dtos.response.UserPreferencesResponse
import com.nickdferrara.fitify.identity.internal.dtos.response.toPreferencesResponse
import com.nickdferrara.fitify.identity.internal.entities.ThemePreference
import com.nickdferrara.fitify.identity.internal.exception.UserNotFoundException
import com.nickdferrara.fitify.identity.internal.repository.UserRepository
import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
internal class IdentityService(
    private val userRepository: UserRepository,
) : IdentityApi {

    override fun findUserById(id: UUID): Result<IdentityUserSummary, DomainError> {
        val user = userRepository.findById(id).orElse(null)
            ?: return Result.Failure(NotFoundError("User not found: $id"))
        return Result.Success(user.toSummary())
    }

    override fun findUserByEmail(email: String): Result<IdentityUserSummary, DomainError> {
        val user = userRepository.findByEmail(email).orElse(null)
            ?: return Result.Failure(NotFoundError("User not found: $email"))
        return Result.Success(user.toSummary())
    }

    fun getPreferences(keycloakId: String): UserPreferencesResponse {
        val user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow { UserNotFoundException(keycloakId) }
        return user.toPreferencesResponse()
    }

    @Transactional
    fun updatePreferences(keycloakId: String, request: UpdatePreferencesRequest): UserPreferencesResponse {
        val user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow { UserNotFoundException(keycloakId) }

        val theme = try {
            ThemePreference.valueOf(request.theme.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid theme: ${request.theme}. Must be one of: ${ThemePreference.entries.joinToString()}")
        }

        user.themePreference = theme
        val saved = userRepository.save(user)
        return saved.toPreferencesResponse()
    }

    private fun com.nickdferrara.fitify.identity.internal.entities.User.toSummary() = IdentityUserSummary(
        id = id!!,
        email = email,
        displayName = "$firstName $lastName",
    )
}
