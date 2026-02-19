package com.nickdferrara.fitify.shared.crypto

import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.GCMSIVBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.slf4j.LoggerFactory
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
    private val properties: EncryptionProperties,
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
        return VERSION_SIV + Base64.getEncoder().encodeToString(output)
    }

    fun encryptNonDeterministic(plaintext: String): String {
        if (plaintext.isBlank()) return plaintext
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return VERSION_CBC + Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(ciphertext: String): String {
        if (ciphertext.isBlank()) return ciphertext

        if (ciphertext.startsWith(VERSION_SIV)) {
            val decoded = Base64.getDecoder().decode(ciphertext.removePrefix(VERSION_SIV))
            return decryptSiv(decoded)
        }

        if (ciphertext.startsWith(VERSION_CBC)) {
            val decoded = Base64.getDecoder().decode(ciphertext.removePrefix(VERSION_CBC))
            return decryptCbc(decoded)
        }

        // Legacy unversioned data — try SIV → CBC → ECB with fallback
        val decoded = Base64.getDecoder().decode(ciphertext)

        try {
            return decryptSiv(decoded)
        } catch (_: Exception) {}

        if (decoded.size > 16 && decoded.size % 16 == 0) {
            try {
                return decryptCbc(decoded)
            } catch (_: Exception) {}
        }

        if (properties.legacyEcbEnabled) {
            try {
                log.warn("Decrypting with deprecated ECB mode — run migration to upgrade this data")
                return decryptEcb(decoded)
            } catch (_: Exception) {}
        }

        throw IllegalStateException("Unable to decrypt ciphertext — data may be corrupted or key may be incorrect")
    }

    fun isSivEncrypted(ciphertext: String): Boolean {
        if (ciphertext.isBlank()) return false
        return try {
            val base64 = if (ciphertext.startsWith(VERSION_SIV)) ciphertext.removePrefix(VERSION_SIV) else ciphertext
            val decoded = Base64.getDecoder().decode(base64)
            decryptSiv(decoded)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isVersioned(ciphertext: String): Boolean =
        ciphertext.startsWith(VERSION_SIV) || ciphertext.startsWith(VERSION_CBC)

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
        private val log = LoggerFactory.getLogger(AesEncryptor::class.java)
        private val DETERMINISTIC_NONCE = ByteArray(12) // fixed zero nonce for deterministic encryption
        const val VERSION_SIV = "v1$"
        const val VERSION_CBC = "v2$"
    }
}
