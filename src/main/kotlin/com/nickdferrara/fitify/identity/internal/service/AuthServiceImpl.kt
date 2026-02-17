package com.nickdferrara.fitify.identity.internal.service

import com.nickdferrara.fitify.identity.PasswordResetRequestedEvent
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.identity.internal.dtos.request.ForgotPasswordRequest
import com.nickdferrara.fitify.identity.internal.dtos.request.RegisterRequest
import com.nickdferrara.fitify.identity.internal.dtos.request.ResetPasswordRequest
import com.nickdferrara.fitify.identity.internal.dtos.response.MessageResponse
import com.nickdferrara.fitify.identity.internal.dtos.response.RegisterResponse
import com.nickdferrara.fitify.identity.internal.extensions.toRegisterResponse
import com.nickdferrara.fitify.identity.internal.entities.PasswordResetToken
import com.nickdferrara.fitify.identity.internal.entities.User
import com.nickdferrara.fitify.identity.internal.exception.EmailAlreadyExistsException
import com.nickdferrara.fitify.identity.internal.exception.IdentityProviderConflictException
import com.nickdferrara.fitify.identity.internal.exception.InvalidTokenException
import com.nickdferrara.fitify.identity.internal.exception.WeakPasswordException
import com.nickdferrara.fitify.identity.internal.gateway.IdentityProviderGateway
import com.nickdferrara.fitify.identity.internal.repository.PasswordResetTokenRepository
import com.nickdferrara.fitify.identity.internal.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

@Service
internal class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val identityProvider: IdentityProviderGateway,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${fitify.security.token-pepper}") private val tokenPepper: String,
) : com.nickdferrara.fitify.identity.internal.service.interfaces.AuthService {

    companion object {
        private val PASSWORD_PATTERN = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,}$")
        private const val TOKEN_EXPIRY_MINUTES = 30L
    }

    @Transactional
    override fun register(request: RegisterRequest): RegisterResponse {
        validatePassword(request.password)

        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException(request.email)
        }

        val keycloakId = try {
            identityProvider.createUser(
                email = request.email,
                password = request.password,
                firstName = request.firstName,
                lastName = request.lastName,
            )
        } catch (e: IdentityProviderConflictException) {
            throw EmailAlreadyExistsException(request.email)
        }

        val user = userRepository.save(
            User(
                keycloakId = keycloakId,
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
            )
        )

        eventPublisher.publishEvent(
            UserRegisteredEvent(
                userId = user.id!!,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
            )
        )

        return user.toRegisterResponse()
    }

    @Transactional
    override fun forgotPassword(request: ForgotPasswordRequest): MessageResponse {
        val user = userRepository.findByEmail(request.email).orElse(null)
            ?: return MessageResponse("If an account exists with that email, a reset link has been sent.")

        val recentCount = passwordResetTokenRepository.countByUserIdAndCreatedAtAfter(
            user.id!!,
            Instant.now().minus(1, ChronoUnit.HOURS),
        )
        if (recentCount >= 3) {
            return MessageResponse("If an account exists with that email, a reset link has been sent.")
        }

        val rawToken = generateToken()
        val tokenHash = hashToken(rawToken)

        passwordResetTokenRepository.save(
            PasswordResetToken(
                userId = user.id!!,
                tokenHash = tokenHash,
                expiresAt = Instant.now().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES),
            )
        )

        val expiresAt = Instant.now().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES)

        eventPublisher.publishEvent(
            PasswordResetRequestedEvent(
                userId = user.id!!,
                email = user.email,
                resetLink = "/reset-password?token=$rawToken",
                expiresAt = expiresAt,
            )
        )

        return MessageResponse("If an account exists with that email, a reset link has been sent.")
    }

    @Transactional
    override fun resetPassword(request: ResetPasswordRequest): MessageResponse {
        validatePassword(request.newPassword)

        val tokenHash = hashToken(request.token)
        val resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { InvalidTokenException("token not found") }

        if (resetToken.usedAt != null) {
            throw InvalidTokenException("token already used")
        }

        if (resetToken.expiresAt.isBefore(Instant.now())) {
            throw InvalidTokenException("token expired")
        }

        val user = userRepository.findById(resetToken.userId)
            .orElseThrow { InvalidTokenException("token not found") }

        identityProvider.updatePassword(user.keycloakId, request.newPassword)
        identityProvider.invalidateSessions(user.keycloakId)

        resetToken.usedAt = Instant.now()
        passwordResetTokenRepository.save(resetToken)

        return MessageResponse("Password has been reset successfully.")
    }

    private fun validatePassword(password: String) {
        if (!PASSWORD_PATTERN.matches(password)) {
            throw WeakPasswordException()
        }
    }

    private fun generateToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest("$tokenPepper$token".toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash)
    }
}
