package com.nickdferrara.fitify.shared.crypto

import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.GCMSIVBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
@EnableConfigurationProperties(EncryptionProperties::class)
class AesEncryptor(
    properties: EncryptionProperties,
) {

    private val secretKey: SecretKeySpec
    private val sivKeyBytes: ByteArray

    init {
        val keyBytes = Base64.getDecoder().decode(properties.key)
        require(keyBytes.size == 32) { "Encryption key must be 32 bytes (AES-256)" }
        secretKey = SecretKeySpec(keyBytes, "AES")
        sivKeyBytes = deriveHkdf(keyBytes, "aes-siv-deterministic".toByteArray(Charsets.UTF_8), 32)
    }

    fun encryptDeterministic(plaintext: String): String {
        if (plaintext.isBlank()) return plaintext
        val cipher = GCMSIVBlockCipher(AESEngine.newInstance())
        val keyParam = KeyParameter(sivKeyBytes)
        val params = AEADParameters(keyParam, 128, DETERMINISTIC_NONCE)
        cipher.init(true, params)
        val plaintextBytes = plaintext.toByteArray(Charsets.UTF_8)
        val output = ByteArray(cipher.getOutputSize(plaintextBytes.size))
        val len = cipher.processBytes(plaintextBytes, 0, plaintextBytes.size, output, 0)
        cipher.doFinal(output, len)
        return Base64.getEncoder().encodeToString(output)
    }

    fun encryptNonDeterministic(plaintext: String): String {
        if (plaintext.isBlank()) return plaintext
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(ciphertext: String): String {
        if (ciphertext.isBlank()) return ciphertext
        val decoded = Base64.getDecoder().decode(ciphertext)

        // Try SIV first (auth tag rejects non-SIV data reliably)
        try {
            return decryptSiv(decoded)
        } catch (_: Exception) {}

        // Then try CBC (IV + ciphertext, must be > 16 bytes and multiple of 16)
        if (decoded.size > 16 && decoded.size % 16 == 0) {
            try {
                return decryptCbc(decoded)
            } catch (_: Exception) {}
        }

        // Legacy ECB fallback
        return decryptEcb(decoded)
    }

    fun isSivEncrypted(ciphertext: String): Boolean {
        if (ciphertext.isBlank()) return false
        return try {
            val decoded = Base64.getDecoder().decode(ciphertext)
            decryptSiv(decoded)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun decryptSiv(decoded: ByteArray): String {
        val cipher = GCMSIVBlockCipher(AESEngine.newInstance())
        val keyParam = KeyParameter(sivKeyBytes)
        val params = AEADParameters(keyParam, 128, DETERMINISTIC_NONCE)
        cipher.init(false, params)
        val output = ByteArray(cipher.getOutputSize(decoded.size))
        val len = cipher.processBytes(decoded, 0, decoded.size, output, 0)
        cipher.doFinal(output, len)
        return String(output, Charsets.UTF_8)
    }

    private fun decryptCbc(decoded: ByteArray): String {
        val iv = decoded.copyOfRange(0, 16)
        val encrypted = decoded.copyOfRange(16, decoded.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    @Deprecated("Legacy ECB decryption — retained for backward compatibility during migration")
    private fun decryptEcb(decoded: ByteArray): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(decoded), Charsets.UTF_8)
    }

    /**
     * Encrypts using legacy AES/ECB — only for testing backward compatibility.
     */
    internal fun encryptEcbForTest(plaintext: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    private fun deriveHkdf(ikm: ByteArray, info: ByteArray, length: Int): ByteArray {
        // HKDF-SHA256 extract
        val hmacSha256 = Mac.getInstance("HmacSHA256")
        val salt = ByteArray(32) // zero salt
        hmacSha256.init(SecretKeySpec(salt, "HmacSHA256"))
        val prk = hmacSha256.doFinal(ikm)

        // HKDF-SHA256 expand
        val result = ByteArray(length)
        var offset = 0
        var counter: Byte = 1
        var previousBlock = ByteArray(0)

        while (offset < length) {
            hmacSha256.init(SecretKeySpec(prk, "HmacSHA256"))
            hmacSha256.update(previousBlock)
            hmacSha256.update(info)
            hmacSha256.update(byteArrayOf(counter))
            previousBlock = hmacSha256.doFinal()
            val toCopy = minOf(previousBlock.size, length - offset)
            System.arraycopy(previousBlock, 0, result, offset, toCopy)
            offset += toCopy
            counter++
        }
        return result
    }

    companion object {
        private val DETERMINISTIC_NONCE = ByteArray(12) // fixed zero nonce for deterministic encryption
    }
}
