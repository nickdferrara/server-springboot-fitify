package com.nickdferrara.fitify.shared.internal.config

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.env.Environment

class SecretsValidatorTest {

    private fun environmentWith(overrides: Map<String, String> = emptyMap()): Environment {
        val defaults = mapOf(
            "fitify.encryption.key" to "cHJvZC1rZXktMzItYnl0ZXMtbG9uZy4u",
            "fitify.security.token-pepper" to "prod-pepper-value",
            "fitify.stripe.secret-key" to "sk_live_real_key",
            "fitify.stripe.webhook-secret" to "whsec_real_secret",
            "fitify.keycloak.client-secret" to "prod-keycloak-secret",
        )
        val merged = defaults + overrides
        val env = mockk<Environment>()
        merged.forEach { (key, value) ->
            every { env.getProperty(key, "") } returns value
        }
        return env
    }

    @Test
    fun `valid production config passes without error`() {
        val validator = SecretsValidator(environmentWith())
        assertDoesNotThrow { validator.validate() }
    }

    @Test
    fun `test default encryption key is rejected`() {
        val validator = SecretsValidator(
            environmentWith(mapOf("fitify.encryption.key" to "dGVzdC1rZXktMzItYnl0ZXMtbG9uZy4u"))
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("encryption.key"))
    }

    @Test
    fun `dev token pepper is rejected`() {
        val validator = SecretsValidator(
            environmentWith(mapOf("fitify.security.token-pepper" to "dev-pepper-value-change-in-prod"))
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("token-pepper"))
    }

    @Test
    fun `placeholder stripe secret key is rejected`() {
        val validator = SecretsValidator(
            environmentWith(mapOf("fitify.stripe.secret-key" to "sk_test_placeholder"))
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("stripe.secret-key"))
    }

    @Test
    fun `blank stripe webhook secret is rejected`() {
        val validator = SecretsValidator(
            environmentWith(mapOf("fitify.stripe.webhook-secret" to ""))
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("stripe.webhook-secret"))
    }

    @Test
    fun `change-me keycloak secret is rejected`() {
        val validator = SecretsValidator(
            environmentWith(mapOf("fitify.keycloak.client-secret" to "change-me"))
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("keycloak.client-secret"))
    }

    @Test
    fun `fitify-dev-secret keycloak secret is rejected`() {
        val validator = SecretsValidator(
            environmentWith(mapOf("fitify.keycloak.client-secret" to "fitify-dev-secret"))
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("keycloak.client-secret"))
    }

    @Test
    fun `multiple errors are reported together`() {
        val validator = SecretsValidator(
            environmentWith(
                mapOf(
                    "fitify.encryption.key" to "dGVzdC1rZXktMzItYnl0ZXMtbG9uZy4u",
                    "fitify.keycloak.client-secret" to "change-me",
                )
            )
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("1."))
        assert(ex.message!!.contains("2."))
    }

    @Test
    fun `stripe test key prefix is rejected`() {
        val validator = SecretsValidator(
            environmentWith(mapOf("fitify.stripe.secret-key" to "sk_test_51J1234abcdef"))
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("stripe.secret-key"))
    }

    @Test
    fun `blank keycloak secret is rejected`() {
        val validator = SecretsValidator(
            environmentWith(mapOf("fitify.keycloak.client-secret" to ""))
        )
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("keycloak.client-secret"))
    }
}
