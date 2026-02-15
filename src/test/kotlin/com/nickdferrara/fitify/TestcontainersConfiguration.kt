package com.nickdferrara.fitify

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> {
        return PostgreSQLContainer("postgres:16-alpine")
    }

    @Bean
    fun keycloakContainer(registry: DynamicPropertyRegistry): KeycloakContainer {
        val keycloak = KeycloakContainer("quay.io/keycloak/keycloak:26.0")
            .withRealmImportFile("test-realm.json")
        registry.add("keycloak.server-url") { keycloak.authServerUrl }
        registry.add("keycloak.realm") { "fitify-test" }
        registry.add("keycloak.client-id") { "fitify-api" }
        registry.add("keycloak.client-secret") { "test-secret" }
        return keycloak
    }
}
