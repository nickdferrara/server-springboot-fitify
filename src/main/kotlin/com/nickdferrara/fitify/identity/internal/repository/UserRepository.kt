package com.nickdferrara.fitify.identity.internal.repository

import com.nickdferrara.fitify.identity.internal.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

internal interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>
    fun findByKeycloakId(keycloakId: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}
