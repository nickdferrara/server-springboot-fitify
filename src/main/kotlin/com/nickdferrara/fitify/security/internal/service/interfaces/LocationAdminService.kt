package com.nickdferrara.fitify.security.internal.service.interfaces

import com.nickdferrara.fitify.security.internal.dtos.response.LocationAdminAssignmentResponse
import org.springframework.security.core.Authentication
import java.util.UUID

internal interface LocationAdminService {
    fun hasAccess(authentication: Authentication, locationId: UUID): Boolean
    fun assignLocationAdmin(keycloakId: String, locationId: UUID): LocationAdminAssignmentResponse
    fun getAssignments(keycloakId: String): List<LocationAdminAssignmentResponse>
    fun removeAssignment(keycloakId: String, locationId: UUID)
}
