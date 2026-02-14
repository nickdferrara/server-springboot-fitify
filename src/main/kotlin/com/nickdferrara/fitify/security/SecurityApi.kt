package com.nickdferrara.fitify.security

import java.util.UUID

interface SecurityApi {
    fun hasLocationAccess(keycloakId: String, locationId: UUID): Boolean
    fun getAdminLocationIds(keycloakId: String): List<UUID>
}
