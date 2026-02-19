package com.nickdferrara.fitify.shared.crypto

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.jdbc.core.JdbcTemplate
import java.util.Base64
import java.util.UUID

class EncryptionMigrationRunnerTest {

    private val key = Base64.getEncoder().encodeToString("test-key-32-bytes-long..........".toByteArray())
    private val encryptor = AesEncryptor(EncryptionProperties(key))
    private val jdbcTemplate = mockk<JdbcTemplate>(relaxed = true)
    private val runner = EncryptionMigrationRunner(jdbcTemplate, encryptor)

    @Test
    fun `migrates ECB-encrypted rows to versioned SIV`() {
        val userId = UUID.randomUUID()
        val ecbEncrypted = encryptor.encryptEcbForTest("user@example.com")

        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("email") }) } returns listOf(
            mapOf("id" to userId, "email" to ecbEncrypted),
        )
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("first_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("last_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("locations") }) } returns emptyList()

        runner.run(DefaultApplicationArguments())

        val expectedSiv = encryptor.encryptDeterministic("user@example.com")
        verify { jdbcTemplate.update("UPDATE users SET email = ? WHERE id = ?", expectedSiv, userId) }
    }

    @Test
    fun `skips rows already encrypted with versioned format`() {
        val userId = UUID.randomUUID()
        val sivEncrypted = encryptor.encryptDeterministic("user@example.com")

        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("email") }) } returns listOf(
            mapOf("id" to userId, "email" to sivEncrypted),
        )
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("first_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("last_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("locations") }) } returns emptyList()

        runner.run(DefaultApplicationArguments())

        verify(exactly = 0) { jdbcTemplate.update(any(), any<String>(), any<UUID>()) }
    }

    @Test
    fun `migrates all tables and columns`() {
        val userId = UUID.randomUUID()
        val locationId = UUID.randomUUID()
        val ecbUser = encryptor.encryptEcbForTest("user@example.com")
        val ecbLocation = encryptor.encryptEcbForTest("gym@example.com")

        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("email") }) } returns listOf(
            mapOf("id" to userId, "email" to ecbUser),
        )
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("first_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("last_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("locations") && it.contains("email") }) } returns listOf(
            mapOf("id" to locationId, "email" to ecbLocation),
        )
        every { jdbcTemplate.queryForList(match { it.contains("locations") && it.contains("phone") }) } returns emptyList()

        runner.run(DefaultApplicationArguments())

        verify { jdbcTemplate.update("UPDATE users SET email = ? WHERE id = ?", any<String>(), userId) }
        verify { jdbcTemplate.update("UPDATE locations SET email = ? WHERE id = ?", any<String>(), locationId) }
    }

    @Test
    fun `unversioned SIV data gets re-encrypted with version prefix`() {
        val userId = UUID.randomUUID()
        // Simulate old unversioned SIV ciphertext (no v1$ prefix)
        val plaintext = "old@example.com"
        val oldSiv = encryptor.encryptDeterministic(plaintext).removePrefix(AesEncryptor.VERSION_SIV)

        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("email") }) } returns listOf(
            mapOf("id" to userId, "email" to oldSiv),
        )
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("first_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("last_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("locations") }) } returns emptyList()

        runner.run(DefaultApplicationArguments())

        val expectedVersioned = encryptor.encryptDeterministic(plaintext)
        assertTrue(expectedVersioned.startsWith(AesEncryptor.VERSION_SIV))
        verify { jdbcTemplate.update("UPDATE users SET email = ? WHERE id = ?", expectedVersioned, userId) }
    }

    @Test
    fun `non-deterministic fields are migrated with v2 prefix`() {
        val userId = UUID.randomUUID()
        val ecbFirstName = encryptor.encryptEcbForTest("John")

        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("email") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("first_name") }) } returns listOf(
            mapOf("id" to userId, "first_name" to ecbFirstName),
        )
        every { jdbcTemplate.queryForList(match { it.contains("users") && it.contains("last_name") }) } returns emptyList()
        every { jdbcTemplate.queryForList(match { it.contains("locations") }) } returns emptyList()

        var capturedReEncrypted: String? = null
        every {
            jdbcTemplate.update(any<String>(), any<String>(), any<UUID>())
        } answers {
            val sql = args[0].toString()
            if (sql.contains("first_name")) {
                @Suppress("UNCHECKED_CAST")
                val varargs = args[1] as Array<Any?>
                capturedReEncrypted = varargs[0] as String
            }
            1
        }

        runner.run(DefaultApplicationArguments())

        assertTrue(capturedReEncrypted != null, "Expected update to be called for first_name")
        assertTrue(
            capturedReEncrypted!!.startsWith(AesEncryptor.VERSION_CBC),
            "Expected v2\$ prefix but got: $capturedReEncrypted",
        )
    }
}
