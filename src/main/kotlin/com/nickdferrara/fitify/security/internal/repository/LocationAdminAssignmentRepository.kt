package com.nickdferrara.fitify.security.internal.repository

import com.nickdferrara.fitify.security.internal.entities.LocationAdminAssignment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface LocationAdminAssignmentRepository : JpaRepository<LocationAdminAssignment, UUID> {
    fun existsByKeycloakIdAndLocationId(keycloakId: String, locationId: UUID): Boolean
    fun findByKeycloakId(keycloakId: String): List<LocationAdminAssignment>
    fun deleteByKeycloakIdAndLocationId(keycloakId: String, locationId: UUID)
}
