package com.nickdferrara.fitify.identity.internal

import com.nickdferrara.fitify.identity.internal.dtos.request.UpdatePreferencesRequest
import com.nickdferrara.fitify.identity.internal.entities.ThemePreference
import com.nickdferrara.fitify.identity.internal.entities.User
import com.nickdferrara.fitify.identity.internal.exception.UserNotFoundException
import com.nickdferrara.fitify.identity.internal.repository.UserRepository
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID

class IdentityServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val identityService = IdentityService(userRepository)

    private fun buildUser(
        id: UUID = UUID.randomUUID(),
        keycloakId: String = UUID.randomUUID().toString(),
        email: String = "user@example.com",
        firstName: String = "John",
        lastName: String = "Doe",
        themePreference: ThemePreference = ThemePreference.SYSTEM,
    ) = User(
        id = id,
        keycloakId = keycloakId,
        email = email,
        firstName = firstName,
        lastName = lastName,
        themePreference = themePreference,
    )

    // --- findUserById Tests ---

    @Test
    fun `findUserById returns summary when user exists`() {
        val id = UUID.randomUUID()
        val user = buildUser(id = id, firstName = "John", lastName = "Doe")
        every { userRepository.findById(id) } returns Optional.of(user)

        val result = identityService.findUserById(id)

        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).value
        assertEquals(id, summary.id)
        assertEquals("user@example.com", summary.email)
        assertEquals("John Doe", summary.displayName)
    }

    @Test
    fun `findUserById returns failure when user does not exist`() {
        val id = UUID.randomUUID()
        every { userRepository.findById(id) } returns Optional.empty()

        val result = identityService.findUserById(id)

        assertTrue(result is Result.Failure)
        val error = (result as Result.Failure).error
        assertTrue(error is NotFoundError)
    }

    // --- findUserByEmail Tests ---

    @Test
    fun `findUserByEmail returns summary when user exists`() {
        val user = buildUser(email = "test@example.com")
        every { userRepository.findByEmail("test@example.com") } returns Optional.of(user)

        val result = identityService.findUserByEmail("test@example.com")

        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).value
        assertEquals("test@example.com", summary.email)
    }

    @Test
    fun `findUserByEmail returns failure when user does not exist`() {
        every { userRepository.findByEmail("unknown@example.com") } returns Optional.empty()

        val result = identityService.findUserByEmail("unknown@example.com")

        assertTrue(result is Result.Failure)
        val error = (result as Result.Failure).error
        assertTrue(error is NotFoundError)
    }

    // --- getPreferences Tests ---

    @Test
    fun `getPreferences returns preferences for existing user`() {
        val keycloakId = UUID.randomUUID().toString()
        val user = buildUser(keycloakId = keycloakId, themePreference = ThemePreference.DARK)
        every { userRepository.findByKeycloakId(keycloakId) } returns Optional.of(user)

        val response = identityService.getPreferences(keycloakId)

        assertEquals("DARK", response.theme)
    }

    @Test
    fun `getPreferences throws UserNotFoundException when user not found`() {
        val keycloakId = UUID.randomUUID().toString()
        every { userRepository.findByKeycloakId(keycloakId) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            identityService.getPreferences(keycloakId)
        }
    }

    // --- updatePreferences Tests ---

    @Test
    fun `updatePreferences updates theme successfully`() {
        val keycloakId = UUID.randomUUID().toString()
        val user = buildUser(keycloakId = keycloakId, themePreference = ThemePreference.SYSTEM)
        every { userRepository.findByKeycloakId(keycloakId) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { firstArg() }

        val response = identityService.updatePreferences(keycloakId, UpdatePreferencesRequest("DARK"))

        assertEquals("DARK", response.theme)
    }

    @Test
    fun `updatePreferences handles case-insensitive theme value`() {
        val keycloakId = UUID.randomUUID().toString()
        val user = buildUser(keycloakId = keycloakId)
        every { userRepository.findByKeycloakId(keycloakId) } returns Optional.of(user)
        every { userRepository.save(any()) } answers { firstArg() }

        val response = identityService.updatePreferences(keycloakId, UpdatePreferencesRequest("light"))

        assertEquals("LIGHT", response.theme)
    }

    @Test
    fun `updatePreferences throws IllegalArgumentException for invalid theme`() {
        val keycloakId = UUID.randomUUID().toString()
        val user = buildUser(keycloakId = keycloakId)
        every { userRepository.findByKeycloakId(keycloakId) } returns Optional.of(user)

        assertThrows<IllegalArgumentException> {
            identityService.updatePreferences(keycloakId, UpdatePreferencesRequest("INVALID"))
        }
    }

    @Test
    fun `updatePreferences throws UserNotFoundException when user not found`() {
        val keycloakId = UUID.randomUUID().toString()
        every { userRepository.findByKeycloakId(keycloakId) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            identityService.updatePreferences(keycloakId, UpdatePreferencesRequest("DARK"))
        }
    }
}
