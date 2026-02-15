package com.nickdferrara.fitify.identity.internal

import com.nickdferrara.fitify.identity.PasswordResetRequestedEvent
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.identity.internal.dtos.request.ForgotPasswordRequest
import com.nickdferrara.fitify.identity.internal.dtos.request.RegisterRequest
import com.nickdferrara.fitify.identity.internal.dtos.request.ResetPasswordRequest
import com.nickdferrara.fitify.identity.internal.entities.PasswordResetToken
import com.nickdferrara.fitify.identity.internal.entities.User
import com.nickdferrara.fitify.identity.internal.exception.EmailAlreadyExistsException
import com.nickdferrara.fitify.identity.internal.exception.InvalidTokenException
import com.nickdferrara.fitify.identity.internal.exception.WeakPasswordException
import com.nickdferrara.fitify.identity.internal.repository.PasswordResetTokenRepository
import com.nickdferrara.fitify.identity.internal.repository.UserRepository
import com.nickdferrara.fitify.identity.internal.service.AuthService
import com.nickdferrara.fitify.identity.internal.service.KeycloakClient
import com.nickdferrara.fitify.identity.internal.service.KeycloakConflictException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordResetTokenRepository = mockk<PasswordResetTokenRepository>()
    private val keycloakClient = mockk<KeycloakClient>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val tokenPepper = "test-pepper"
    private val authService = AuthService(userRepository, passwordResetTokenRepository, keycloakClient, eventPublisher, tokenPepper)

    private fun buildUser(
        id: UUID = UUID.randomUUID(),
        keycloakId: String = UUID.randomUUID().toString(),
        email: String = "user@example.com",
        firstName: String = "John",
        lastName: String = "Doe",
    ) = User(
        id = id,
        keycloakId = keycloakId,
        email = email,
        firstName = firstName,
        lastName = lastName,
    )

    // --- Registration Tests ---

    @Test
    fun `register creates user and publishes event`() {
        val request = RegisterRequest(
            email = "new@example.com",
            password = "Strong1@pass",
            firstName = "Jane",
            lastName = "Doe",
        )
        val keycloakId = UUID.randomUUID().toString()
        val savedId = UUID.randomUUID()

        every { userRepository.existsByEmail(request.email) } returns false
        every { keycloakClient.createUser(request.email, request.password, request.firstName, request.lastName) } returns keycloakId
        every { userRepository.save(any()) } answers {
            val u = firstArg<User>()
            User(
                id = savedId,
                keycloakId = u.keycloakId,
                email = u.email,
                firstName = u.firstName,
                lastName = u.lastName,
            )
        }

        val response = authService.register(request)

        assertEquals(savedId, response.id)
        assertEquals("new@example.com", response.email)
        assertEquals("Jane", response.firstName)

        val eventSlot = slot<UserRegisteredEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(savedId, eventSlot.captured.userId)
        assertEquals("new@example.com", eventSlot.captured.email)
    }

    @Test
    fun `register throws EmailAlreadyExistsException when email exists in local DB`() {
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "Strong1@pass",
            firstName = "Jane",
            lastName = "Doe",
        )

        every { userRepository.existsByEmail(request.email) } returns true

        assertThrows<EmailAlreadyExistsException> {
            authService.register(request)
        }
    }

    @Test
    fun `register throws EmailAlreadyExistsException when Keycloak returns 409`() {
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "Strong1@pass",
            firstName = "Jane",
            lastName = "Doe",
        )

        every { userRepository.existsByEmail(request.email) } returns false
        every { keycloakClient.createUser(any(), any(), any(), any()) } throws KeycloakConflictException("exists")

        assertThrows<EmailAlreadyExistsException> {
            authService.register(request)
        }
    }

    @Test
    fun `register throws WeakPasswordException for weak password`() {
        val request = RegisterRequest(
            email = "new@example.com",
            password = "weak",
            firstName = "Jane",
            lastName = "Doe",
        )

        assertThrows<WeakPasswordException> {
            authService.register(request)
        }
    }

    // --- Forgot Password Tests ---

    @Test
    fun `forgotPassword creates token and publishes event when user exists`() {
        val user = buildUser()
        every { userRepository.findByEmail(user.email) } returns Optional.of(user)
        every { passwordResetTokenRepository.countByUserIdAndCreatedAtAfter(user.id!!, any()) } returns 0L
        every { passwordResetTokenRepository.save(any()) } answers { firstArg() }

        val response = authService.forgotPassword(ForgotPasswordRequest(user.email))

        assertNotNull(response.message)

        val eventSlot = slot<PasswordResetRequestedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(user.id, eventSlot.captured.userId)
        assertEquals(user.email, eventSlot.captured.email)
        assertNotNull(eventSlot.captured.resetLink)
        assertTrue(eventSlot.captured.resetLink.startsWith("/reset-password?token="))
        assertNotNull(eventSlot.captured.expiresAt)
    }

    @Test
    fun `forgotPassword returns success message even when user not found`() {
        every { userRepository.findByEmail("unknown@example.com") } returns Optional.empty()

        val response = authService.forgotPassword(ForgotPasswordRequest("unknown@example.com"))

        assertNotNull(response.message)
        verify(exactly = 0) { eventPublisher.publishEvent(any<PasswordResetRequestedEvent>()) }
    }

    // --- Reset Password Tests ---

    @Test
    fun `resetPassword succeeds with valid token`() {
        val user = buildUser()
        val tokenHash = "hashed-token"
        val resetToken = PasswordResetToken(
            id = UUID.randomUUID(),
            userId = user.id!!,
            tokenHash = tokenHash,
            expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES),
        )

        // We need to mock the hash computation. Since we can't control SecureRandom,
        // we mock the repository to return the token for any hash.
        every { passwordResetTokenRepository.findByTokenHash(any()) } returns Optional.of(resetToken)
        every { userRepository.findById(user.id!!) } returns Optional.of(user)
        every { keycloakClient.updatePassword(user.keycloakId, "NewStrong1@pass") } returns Unit
        every { keycloakClient.invalidateSessions(user.keycloakId) } returns Unit
        every { passwordResetTokenRepository.save(any()) } answers { firstArg() }

        val response = authService.resetPassword(ResetPasswordRequest("raw-token", "NewStrong1@pass"))

        assertNotNull(response.message)
        verify { keycloakClient.updatePassword(user.keycloakId, "NewStrong1@pass") }
        verify { keycloakClient.invalidateSessions(user.keycloakId) }
    }

    @Test
    fun `resetPassword throws InvalidTokenException for expired token`() {
        val resetToken = PasswordResetToken(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            tokenHash = "hashed",
            expiresAt = Instant.now().minus(1, ChronoUnit.HOURS),
        )

        every { passwordResetTokenRepository.findByTokenHash(any()) } returns Optional.of(resetToken)

        assertThrows<InvalidTokenException> {
            authService.resetPassword(ResetPasswordRequest("raw-token", "NewStrong1@pass"))
        }
    }

    @Test
    fun `resetPassword throws InvalidTokenException for used token`() {
        val resetToken = PasswordResetToken(
            id = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            tokenHash = "hashed",
            expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES),
            usedAt = Instant.now().minus(5, ChronoUnit.MINUTES),
        )

        every { passwordResetTokenRepository.findByTokenHash(any()) } returns Optional.of(resetToken)

        assertThrows<InvalidTokenException> {
            authService.resetPassword(ResetPasswordRequest("raw-token", "NewStrong1@pass"))
        }
    }

    @Test
    fun `resetPassword throws InvalidTokenException for nonexistent token`() {
        every { passwordResetTokenRepository.findByTokenHash(any()) } returns Optional.empty()

        assertThrows<InvalidTokenException> {
            authService.resetPassword(ResetPasswordRequest("bad-token", "NewStrong1@pass"))
        }
    }

    @Test
    fun `resetPassword throws WeakPasswordException for weak new password`() {
        assertThrows<WeakPasswordException> {
            authService.resetPassword(ResetPasswordRequest("raw-token", "weak"))
        }
    }
}
