package com.nickdferrara.fitify.shared.crypto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        val base64Part = encrypted.removePrefix(AesEncryptor.VERSION_SIV)
        val decoded = Base64.getDecoder().decode(base64Part)
        decoded[0] = (decoded[0].toInt() xor 0xFF).toByte()
        val tampered = AesEncryptor.VERSION_SIV + Base64.getEncoder().encodeToString(decoded)

        assertFalse(encryptor.isSivEncrypted(tampered))
    }

    @Test
    fun `SIV output includes 16-byte authentication tag`() {
        val plaintext = "test@example.com"
        val encrypted = encryptor.encryptDeterministic(plaintext)
        val base64Part = encrypted.removePrefix(AesEncryptor.VERSION_SIV)
        val decoded = Base64.getDecoder().decode(base64Part)

        assertEquals(plaintext.toByteArray(Charsets.UTF_8).size + 16, decoded.size)
    }

    @Test
    fun `deterministic output starts with v1 prefix`() {
        val encrypted = encryptor.encryptDeterministic("test@example.com")

        assertTrue(encrypted.startsWith(AesEncryptor.VERSION_SIV))
    }

    @Test
    fun `non-deterministic output starts with v2 prefix`() {
        val encrypted = encryptor.encryptNonDeterministic("John")

        assertTrue(encrypted.startsWith(AesEncryptor.VERSION_CBC))
    }

    @Test
    fun `corrupted versioned SIV ciphertext throws`() {
        val corrupted = AesEncryptor.VERSION_SIV + Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3, 4))

        assertThrows<Exception> { encryptor.decrypt(corrupted) }
    }

    @Test
    fun `corrupted versioned CBC ciphertext throws`() {
        val corrupted = AesEncryptor.VERSION_CBC + Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3, 4))

        assertThrows<Exception> { encryptor.decrypt(corrupted) }
    }

    @Test
    fun `completely invalid ciphertext throws IllegalStateException`() {
        // Not valid base64 for any cipher mode — will fail all legacy fallbacks
        val invalid = Base64.getEncoder().encodeToString(byteArrayOf(0, 0, 0))

        assertThrows<IllegalStateException> { encryptor.decrypt(invalid) }
    }

    @Test
    fun `isVersioned returns true for v1 prefix`() {
        val encrypted = encryptor.encryptDeterministic("test@example.com")

        assertTrue(encryptor.isVersioned(encrypted))
    }

    @Test
    fun `isVersioned returns true for v2 prefix`() {
        val encrypted = encryptor.encryptNonDeterministic("John")

        assertTrue(encryptor.isVersioned(encrypted))
    }

    @Test
    fun `isVersioned returns false for legacy unversioned data`() {
        val ecbEncrypted = encryptor.encryptEcbForTest("test@example.com")

        assertFalse(encryptor.isVersioned(ecbEncrypted))
    }

    @Test
    fun `legacy ECB throws when legacyEcbEnabled is false`() {
        val encryptorNoEcb = AesEncryptor(EncryptionProperties(key, legacyEcbEnabled = false))
        val ecbEncrypted = encryptor.encryptEcbForTest("test@example.com")

        assertThrows<IllegalStateException> { encryptorNoEcb.decrypt(ecbEncrypted) }
    }
}
