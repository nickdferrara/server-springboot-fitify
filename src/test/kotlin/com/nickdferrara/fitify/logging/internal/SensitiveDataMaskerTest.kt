package com.nickdferrara.fitify.logging.internal

import com.nickdferrara.fitify.logging.Sensitive
import com.nickdferrara.fitify.logging.internal.service.SensitiveDataMasker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SensitiveDataMaskerTest {

    private val masker = SensitiveDataMasker()

    data class LoginRequest(
        val email: String,
        val password: String,
    )

    data class TokenData(
        val userId: String,
        val token: String,
    )

    data class PaymentInfo(
        val amount: Long,
        val creditCardNumber: String,
        val ssn: String,
    )

    data class SafeData(
        val name: String,
        val age: Int,
    )

    data class AnnotatedData(
        val name: String,
        @field:Sensitive
        val internalCode: String,
    )

    @Test
    fun `masks password fields`() {
        val result = masker.maskArgs(arrayOf(LoginRequest("test@email.com", "secret123")))

        assertEquals(1, result.size)
        assertTrue(result[0].contains("***MASKED***"))
        assertTrue(result[0].contains("test@email.com"))
        assertFalse(result[0].contains("secret123"))
    }

    @Test
    fun `masks token fields`() {
        val result = masker.maskArgs(arrayOf(TokenData("user-1", "jwt-abc-123")))

        assertEquals(1, result.size)
        assertTrue(result[0].contains("***MASKED***"))
        assertTrue(result[0].contains("user-1"))
        assertFalse(result[0].contains("jwt-abc-123"))
    }

    @Test
    fun `masks credit card and SSN fields`() {
        val result = masker.maskArgs(arrayOf(PaymentInfo(1000, "4111111111111111", "123-45-6789")))

        assertEquals(1, result.size)
        assertTrue(result[0].contains("amount=1000"))
        assertFalse(result[0].contains("4111111111111111"))
        assertFalse(result[0].contains("123-45-6789"))
    }

    @Test
    fun `preserves non-sensitive fields`() {
        val result = masker.maskArgs(arrayOf(SafeData("Alice", 30)))

        assertEquals(1, result.size)
        assertEquals("SafeData(name=Alice, age=30)", result[0])
    }

    @Test
    fun `masks fields annotated with @Sensitive`() {
        val result = masker.maskArgs(arrayOf(AnnotatedData("visible", "hidden-value")))

        assertEquals(1, result.size)
        assertTrue(result[0].contains("name=visible"))
        assertTrue(result[0].contains("***MASKED***"))
        assertFalse(result[0].contains("hidden-value"))
    }

    @Test
    fun `handles null arguments`() {
        val result = masker.maskArgs(arrayOf(null))

        assertEquals(1, result.size)
        assertEquals("null", result[0])
    }

    @Test
    fun `handles multiple arguments`() {
        val result = masker.maskArgs(arrayOf(
            SafeData("Bob", 25),
            LoginRequest("bob@test.com", "pass"),
        ))

        assertEquals(2, result.size)
        assertEquals("SafeData(name=Bob, age=25)", result[0])
        assertTrue(result[1].contains("***MASKED***"))
    }
}
