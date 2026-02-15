package com.nickdferrara.fitify.identity.internal.gateway

internal interface IdentityProviderGateway {
    fun createUser(email: String, password: String, firstName: String, lastName: String): String
    fun updatePassword(keycloakId: String, newPassword: String)
    fun invalidateSessions(keycloakId: String)
}
