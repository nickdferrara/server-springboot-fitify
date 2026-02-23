package com.nickdferrara.fitify.shared.internal.config

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment

class SecretsValidatorIntegrationTest {

    private fun environmentWith(props: Map<String, Any>): StandardEnvironment {
        val env = StandardEnvironment()
        env.propertySources.addFirst(MapPropertySource("test", props))
        return env
    }

    @Test
    fun `all dev default secrets are caught in a single validation pass`() {
        val env = environmentWith(
            mapOf(
                "fitify.encryption.key" to "dGVzdC1rZXktMzItYnl0ZXMtbG9uZy4u",
                "fitify.security.token-pepper" to "dev-pepper-value",
                "fitify.stripe.secret-key" to "sk_test_placeholder",
                "fitify.stripe.webhook-secret" to "whsec_placeholder",
                "fitify.keycloak.client-secret" to "change-me",
            )
        )
        val validator = SecretsValidator(env)
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        val message = ex.message!!
        assert(message.contains("encryption.key"))
        assert(message.contains("token-pepper"))
        assert(message.contains("stripe.secret-key"))
        assert(message.contains("stripe.webhook-secret"))
        assert(message.contains("keycloak.client-secret"))
    }

    @Test
    fun `real stripe test key is caught even without placeholder text`() {
        val env = environmentWith(
            mapOf(
                "fitify.encryption.key" to "cHJvZC1rZXktMzItYnl0ZXMtbG9uZy4u",
                "fitify.security.token-pepper" to "prod-pepper-value",
                "fitify.stripe.secret-key" to "sk_test_51J1234abcdef",
                "fitify.stripe.webhook-secret" to "whsec_real_secret",
                "fitify.keycloak.client-secret" to "prod-keycloak-secret",
            )
        )
        val validator = SecretsValidator(env)
        val ex = assertThrows<IllegalStateException> { validator.validate() }
        assert(ex.message!!.contains("stripe.secret-key"))
    }

    @Test
    fun `valid production secrets pass all checks`() {
        val env = environmentWith(
            mapOf(
                "fitify.encryption.key" to "cHJvZC1rZXktMzItYnl0ZXMtbG9uZy4u",
                "fitify.security.token-pepper" to "prod-pepper-value",
                "fitify.stripe.secret-key" to "sk_live_real_key",
                "fitify.stripe.webhook-secret" to "whsec_real_secret",
                "fitify.keycloak.client-secret" to "prod-keycloak-secret",
            )
        )
        val validator = SecretsValidator(env)
        assertDoesNotThrow { validator.validate() }
    }
}
