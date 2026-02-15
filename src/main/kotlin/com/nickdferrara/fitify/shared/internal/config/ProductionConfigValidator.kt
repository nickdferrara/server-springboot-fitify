package com.nickdferrara.fitify.shared.internal.config

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
@Profile("prod")
internal class ProductionConfigValidator(
    private val environment: Environment,
) {

    @EventListener(ApplicationReadyEvent::class)
    fun validate() {
        val errors = mutableListOf<String>()

        val encryptionKey = environment.getProperty("fitify.encryption.key", "")
        if (encryptionKey == "dGVzdC1rZXktMzItYnl0ZXMtbG9uZy4u") {
            errors.add("fitify.encryption.key is set to the test default — generate a unique Base64-encoded 32-byte key")
        }

        val tokenPepper = environment.getProperty("fitify.security.token-pepper", "")
        if (tokenPepper.startsWith("dev-pepper")) {
            errors.add("fitify.security.token-pepper is set to a dev default — use a unique random value")
        }

        val stripeSecretKey = environment.getProperty("fitify.stripe.secret-key", "")
        if (stripeSecretKey.isBlank() || stripeSecretKey.contains("placeholder")) {
            errors.add("fitify.stripe.secret-key is blank or a placeholder — set your live Stripe secret key")
        }

        val stripeWebhookSecret = environment.getProperty("fitify.stripe.webhook-secret", "")
        if (stripeWebhookSecret.isBlank() || stripeWebhookSecret.contains("placeholder")) {
            errors.add("fitify.stripe.webhook-secret is blank or a placeholder — set your Stripe webhook secret")
        }

        val keycloakSecret = environment.getProperty("fitify.keycloak.client-secret", "")
        if (keycloakSecret == "change-me" || keycloakSecret == "fitify-dev-secret") {
            errors.add("fitify.keycloak.client-secret is set to a dev default — use the secret from your Keycloak client")
        }

        if (errors.isNotEmpty()) {
            val message = buildString {
                appendLine("Production configuration validation failed:")
                errors.forEachIndexed { index, error ->
                    appendLine("  ${index + 1}. $error")
                }
            }
            throw IllegalStateException(message)
        }
    }
}
