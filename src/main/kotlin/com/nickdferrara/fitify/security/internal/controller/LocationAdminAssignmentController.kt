package com.nickdferrara.fitify.security.internal.controller

import com.nickdferrara.fitify.security.internal.dtos.request.AssignLocationAdminRequest
import com.nickdferrara.fitify.security.internal.dtos.response.LocationAdminAssignmentResponse
import com.nickdferrara.fitify.security.internal.service.LocationAdminService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/location-admins")
@PreAuthorize("hasRole('ADMIN')")
internal class LocationAdminAssignmentController(
    private val locationAdminService: LocationAdminService,
) {

    @PostMapping
    fun assignLocationAdmin(
        @Valid @RequestBody request: AssignLocationAdminRequest,
    ): ResponseEntity<LocationAdminAssignmentResponse> {
        val response = locationAdminService.assignLocationAdmin(request.keycloakId, request.locationId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{keycloakId}/locations")
    fun getAssignments(
        @PathVariable keycloakId: String,
    ): ResponseEntity<List<LocationAdminAssignmentResponse>> {
        return ResponseEntity.ok(locationAdminService.getAssignments(keycloakId))
    }

    @DeleteMapping("/{keycloakId}/locations/{locationId}")
    fun removeAssignment(
        @PathVariable keycloakId: String,
        @PathVariable locationId: UUID,
    ): ResponseEntity<Void> {
        locationAdminService.removeAssignment(keycloakId, locationId)
        return ResponseEntity.noContent().build()
    }
}
