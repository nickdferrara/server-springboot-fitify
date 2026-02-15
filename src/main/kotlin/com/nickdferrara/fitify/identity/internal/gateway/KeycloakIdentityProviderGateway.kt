package com.nickdferrara.fitify.identity.internal.gateway

import com.nickdferrara.fitify.identity.internal.config.KeycloakProperties
import com.nickdferrara.fitify.identity.internal.exception.IdentityProviderConflictException
import com.nickdferrara.fitify.identity.internal.exception.IdentityProviderException
import jakarta.ws.rs.ClientErrorException
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(KeycloakProperties::class)
internal class KeycloakIdentityProviderGateway(
    private val properties: KeycloakProperties,
) : IdentityProviderGateway {

    private val keycloak by lazy {
        KeycloakBuilder.builder()
            .serverUrl(properties.serverUrl)
            .realm(properties.realm)
            .clientId(properties.clientId)
            .clientSecret(properties.clientSecret)
            .grantType("client_credentials")
            .build()
    }

    private fun realmResource() = keycloak.realm(properties.realm)

    override fun createUser(email: String, password: String, firstName: String, lastName: String): String {
        val user = UserRepresentation().apply {
            this.username = email
            this.email = email
            this.firstName = firstName
            this.lastName = lastName
            this.isEnabled = true
            this.isEmailVerified = true
            this.credentials = listOf(
                CredentialRepresentation().apply {
                    this.type = CredentialRepresentation.PASSWORD
                    this.value = password
                    this.isTemporary = false
                }
            )
        }

        val response = realmResource().users().create(user)

        return when (response.status) {
            201 -> {
                val locationHeader = response.location.toString()
                locationHeader.substringAfterLast("/")
            }
            409 -> throw IdentityProviderConflictException("User already exists: $email")
            else -> throw IdentityProviderException("Failed to create user: ${response.status}")
        }
    }

    override fun updatePassword(keycloakId: String, newPassword: String) {
        try {
            val credential = CredentialRepresentation().apply {
                type = CredentialRepresentation.PASSWORD
                value = newPassword
                isTemporary = false
            }
            realmResource().users().get(keycloakId).resetPassword(credential)
        } catch (e: ClientErrorException) {
            throw IdentityProviderException("Failed to update password: ${e.message}")
        }
    }

    override fun invalidateSessions(keycloakId: String) {
        try {
            realmResource().users().get(keycloakId).logout()
        } catch (e: ClientErrorException) {
            throw IdentityProviderException("Failed to invalidate sessions: ${e.message}")
        }
    }
}
