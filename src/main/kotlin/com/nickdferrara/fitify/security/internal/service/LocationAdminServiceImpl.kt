package com.nickdferrara.fitify.security.internal.service

import com.nickdferrara.fitify.security.SecurityApi
import com.nickdferrara.fitify.security.internal.dtos.response.LocationAdminAssignmentResponse
import com.nickdferrara.fitify.security.internal.entities.LocationAdminAssignment
import com.nickdferrara.fitify.security.internal.exception.AssignmentAlreadyExistsException
import com.nickdferrara.fitify.security.internal.exception.AssignmentNotFoundException
import com.nickdferrara.fitify.security.internal.repository.LocationAdminAssignmentRepository
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service("locationAdminService")
internal class LocationAdminServiceImpl(
    private val repository: LocationAdminAssignmentRepository,
) : com.nickdferrara.fitify.security.internal.service.interfaces.LocationAdminService, SecurityApi {

    override fun hasAccess(authentication: Authentication, locationId: UUID): Boolean {
        val authorities = authentication.authorities.map { it.authority }

        if (authorities.contains("ROLE_ADMIN")) return true

        if (authorities.contains("ROLE_LOCATION_ADMIN")) {
            val keycloakId = extractKeycloakId(authentication) ?: return false
            return repository.existsByKeycloakIdAndLocationId(keycloakId, locationId)
        }

        return false
    }

    override fun hasLocationAccess(keycloakId: String, locationId: UUID): Boolean {
        return repository.existsByKeycloakIdAndLocationId(keycloakId, locationId)
    }

    override fun getAdminLocationIds(keycloakId: String): List<UUID> {
        return repository.findByKeycloakId(keycloakId).map { it.locationId }
    }

    @Transactional
    override fun assignLocationAdmin(keycloakId: String, locationId: UUID): LocationAdminAssignmentResponse {
        if (repository.existsByKeycloakIdAndLocationId(keycloakId, locationId)) {
            throw AssignmentAlreadyExistsException(keycloakId, locationId)
        }

        val assignment = repository.save(
            LocationAdminAssignment(
                keycloakId = keycloakId,
                locationId = locationId,
            )
        )

        return assignment.toResponse()
    }

    override fun getAssignments(keycloakId: String): List<LocationAdminAssignmentResponse> {
        return repository.findByKeycloakId(keycloakId).map { it.toResponse() }
    }

    @Transactional
    override fun removeAssignment(keycloakId: String, locationId: UUID) {
        if (!repository.existsByKeycloakIdAndLocationId(keycloakId, locationId)) {
            throw AssignmentNotFoundException(keycloakId, locationId)
        }
        repository.deleteByKeycloakIdAndLocationId(keycloakId, locationId)
    }

    private fun extractKeycloakId(authentication: Authentication): String? {
        val principal = authentication.principal
        return if (principal is Jwt) principal.subject else null
    }

    private fun LocationAdminAssignment.toResponse() = LocationAdminAssignmentResponse(
        id = id!!,
        keycloakId = keycloakId,
        locationId = locationId,
        createdAt = createdAt!!,
    )
}
