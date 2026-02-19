package com.nickdferrara.fitify.shared.crypto

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    fun `migrates ECB-encrypted rows to SIV`() {
        val userId = UUID.randomUUID()
        val ecbEncrypted = encryptor.encryptEcbForTest("user@example.com")

        every { jdbcTemplate.queryForList(match { it.contains("users") }) } returns listOf(
            mapOf("id" to userId, "email" to ecbEncrypted),
        )
        every { jdbcTemplate.queryForList(match { it.contains("locations") }) } returns emptyList()

        runner.run(DefaultApplicationArguments())

        val expectedSiv = encryptor.encryptDeterministic("user@example.com")
        verify { jdbcTemplate.update("UPDATE users SET email = ? WHERE id = ?", expectedSiv, userId) }
    }

    @Test
    fun `skips rows already encrypted with SIV`() {
        val userId = UUID.randomUUID()
        val sivEncrypted = encryptor.encryptDeterministic("user@example.com")

        every { jdbcTemplate.queryForList(match { it.contains("users") }) } returns listOf(
            mapOf("id" to userId, "email" to sivEncrypted),
        )
        every { jdbcTemplate.queryForList(match { it.contains("locations") }) } returns emptyList()

        runner.run(DefaultApplicationArguments())

        verify(exactly = 0) { jdbcTemplate.update(any(), any<String>(), any<UUID>()) }
    }

    @Test
    fun `migrates both tables`() {
        val userId = UUID.randomUUID()
        val locationId = UUID.randomUUID()
        val ecbUser = encryptor.encryptEcbForTest("user@example.com")
        val ecbLocation = encryptor.encryptEcbForTest("gym@example.com")

        every { jdbcTemplate.queryForList(match { it.contains("users") }) } returns listOf(
            mapOf("id" to userId, "email" to ecbUser),
        )
        every { jdbcTemplate.queryForList(match { it.contains("locations") }) } returns listOf(
            mapOf("id" to locationId, "email" to ecbLocation),
        )

        runner.run(DefaultApplicationArguments())

        verify { jdbcTemplate.update("UPDATE users SET email = ? WHERE id = ?", any<String>(), userId) }
        verify { jdbcTemplate.update("UPDATE locations SET email = ? WHERE id = ?", any<String>(), locationId) }
    }
}
