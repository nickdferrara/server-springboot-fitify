package com.nickdferrara.fitify.shared.crypto

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
@EnableConfigurationProperties(EncryptionProperties::class)
class AesEncryptor(
    properties: EncryptionProperties,
) {

    private val secretKey: SecretKeySpec

    init {
        val keyBytes = Base64.getDecoder().decode(properties.key)
        require(keyBytes.size == 32) { "Encryption key must be 32 bytes (AES-256)" }
        secretKey = SecretKeySpec(keyBytes, "AES")
    }

    fun encryptDeterministic(plaintext: String): String {
        if (plaintext.isBlank()) return plaintext
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
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

        return if (decoded.size > 16 && decoded.size % 16 == 0) {
            // Try CBC first (IV + ciphertext)
            try {
                decryptCbc(decoded)
            } catch (_: Exception) {
                decryptEcb(decoded)
            }
        } else {
            decryptEcb(decoded)
        }
    }

    private fun decryptCbc(decoded: ByteArray): String {
        val iv = decoded.copyOfRange(0, 16)
        val encrypted = decoded.copyOfRange(16, decoded.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    private fun decryptEcb(decoded: ByteArray): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return String(cipher.doFinal(decoded), Charsets.UTF_8)
    }
}
