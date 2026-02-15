package com.nickdferrara.fitify.identity.internal.gateway

import com.nickdferrara.fitify.identity.internal.exception.IdentityProviderConflictException
import dasniko.testcontainers.keycloak.KeycloakContainer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
@ActiveProfiles("integration")
@EnabledIf("isDockerAvailable")
internal class KeycloakIdentityProviderGatewayIntegrationTest {

    companion object {
        private val keycloak: KeycloakContainer by lazy {
            KeycloakContainer("quay.io/keycloak/keycloak:26.0")
                .withRealmImportFile("test-realm.json")
                .apply { start() }
        }

        @JvmStatic
        @DynamicPropertySource
        fun keycloakProperties(registry: DynamicPropertyRegistry) {
            if (!isDockerAvailable()) return
            registry.add("keycloak.server-url") { keycloak.authServerUrl }
            registry.add("keycloak.realm") { "fitify-test" }
            registry.add("keycloak.client-id") { "fitify-api" }
            registry.add("keycloak.client-secret") { "test-secret" }
        }

        @JvmStatic
        fun isDockerAvailable(): Boolean {
            return try {
                val process = ProcessBuilder("docker", "info").start()
                process.waitFor() == 0
            } catch (_: Exception) {
                false
            }
        }
    }

    @Autowired
    lateinit var gateway: KeycloakIdentityProviderGateway

    @Test
    fun `createUser creates a new user and returns keycloak ID`() {
        val keycloakId = gateway.createUser(
            email = "newuser-${System.currentTimeMillis()}@fitify.com",
            password = "test-password-123",
            firstName = "New",
            lastName = "User",
        )

        assertThat(keycloakId).isNotBlank()
    }

    @Test
    fun `createUser throws conflict when user already exists`() {
        val email = "duplicate-${System.currentTimeMillis()}@fitify.com"
        gateway.createUser(email, "password1", "First", "Last")

        assertThatThrownBy {
            gateway.createUser(email, "password2", "First", "Last")
        }.isInstanceOf(IdentityProviderConflictException::class.java)
    }

    @Test
    fun `updatePassword succeeds for existing user`() {
        val email = "pwdtest-${System.currentTimeMillis()}@fitify.com"
        val keycloakId = gateway.createUser(email, "old-password", "Pwd", "Test")

        gateway.updatePassword(keycloakId, "new-password-123")

        // No exception thrown means success
    }
}
