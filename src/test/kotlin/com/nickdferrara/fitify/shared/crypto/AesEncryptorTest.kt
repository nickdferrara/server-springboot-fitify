package com.nickdferrara.fitify.shared.crypto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Base64

class AesEncryptorTest {

    private val key = Base64.getEncoder().encodeToString("test-key-32-bytes-long..........".toByteArray())
    private val encryptor = AesEncryptor(EncryptionProperties(key))

    @Test
    fun `deterministic encryption produces same output for same input`() {
        val plaintext = "user@example.com"
        val encrypted1 = encryptor.encryptDeterministic(plaintext)
        val encrypted2 = encryptor.encryptDeterministic(plaintext)

        assertEquals(encrypted1, encrypted2)
    }

    @Test
    fun `non-deterministic encryption produces different output for same input`() {
        val plaintext = "John"
        val encrypted1 = encryptor.encryptNonDeterministic(plaintext)
        val encrypted2 = encryptor.encryptNonDeterministic(plaintext)

        assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    fun `decrypt reverses deterministic encryption`() {
        val plaintext = "user@example.com"
        val encrypted = encryptor.encryptDeterministic(plaintext)
        val decrypted = encryptor.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `decrypt reverses non-deterministic encryption`() {
        val plaintext = "John Doe"
        val encrypted = encryptor.encryptNonDeterministic(plaintext)
        val decrypted = encryptor.decrypt(encrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `blank string is returned as-is for deterministic encryption`() {
        assertEquals("", encryptor.encryptDeterministic(""))
        assertEquals(" ", encryptor.decrypt(" "))
    }

    @Test
    fun `blank string is returned as-is for non-deterministic encryption`() {
        assertEquals("", encryptor.encryptNonDeterministic(""))
    }

    @Test
    fun `encrypted output differs from plaintext`() {
        val plaintext = "sensitive@email.com"
        val encrypted = encryptor.encryptDeterministic(plaintext)

        assertNotEquals(plaintext, encrypted)
    }

    @Test
    fun `decrypt handles legacy ECB-encrypted data`() {
        val plaintext = "legacy@example.com"
        val ecbEncrypted = encryptor.encryptEcbForTest(plaintext)
        val decrypted = encryptor.decrypt(ecbEncrypted)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `isSivEncrypted returns true for SIV-encrypted data`() {
        val encrypted = encryptor.encryptDeterministic("test@example.com")

        assertTrue(encryptor.isSivEncrypted(encrypted))
    }

    @Test
    fun `isSivEncrypted returns false for ECB-encrypted data`() {
        val ecbEncrypted = encryptor.encryptEcbForTest("test@example.com")

        assertFalse(encryptor.isSivEncrypted(ecbEncrypted))
    }

    @Test
    fun `isSivEncrypted returns false for blank string`() {
        assertFalse(encryptor.isSivEncrypted(""))
        assertFalse(encryptor.isSivEncrypted(" "))
    }

    @Test
    fun `SIV rejects tampered ciphertext`() {
        val encrypted = encryptor.encryptDeterministic("test@example.com")
        val decoded = Base64.getDecoder().decode(encrypted)
        decoded[0] = (decoded[0].toInt() xor 0xFF).toByte()
        val tampered = Base64.getEncoder().encodeToString(decoded)

        assertFalse(encryptor.isSivEncrypted(tampered))
    }

    @Test
    fun `SIV output includes 16-byte authentication tag`() {
        val plaintext = "test@example.com"
        val encrypted = encryptor.encryptDeterministic(plaintext)
        val decoded = Base64.getDecoder().decode(encrypted)

        assertEquals(plaintext.toByteArray(Charsets.UTF_8).size + 16, decoded.size)
    }
}
