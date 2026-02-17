package com.nickdferrara.fitify.location.internal.controller

import com.nickdferrara.fitify.location.internal.dtos.request.CreateLocationRequest
import com.nickdferrara.fitify.location.internal.dtos.request.UpdateLocationRequest
import com.nickdferrara.fitify.location.internal.dtos.response.LocationResponse
import com.nickdferrara.fitify.location.internal.service.interfaces.LocationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/locations")
@PreAuthorize("hasRole('ADMIN')")
internal class AdminLocationController(
    private val locationService: LocationService,
) {

    @PostMapping
    fun createLocation(@Valid @RequestBody request: CreateLocationRequest): ResponseEntity<LocationResponse> {
        val response = locationService.createLocation(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun listLocations(): ResponseEntity<List<LocationResponse>> {
        return ResponseEntity.ok(locationService.findAll())
    }

    @GetMapping("/{locationId}")
    fun findLocation(@PathVariable locationId: UUID): ResponseEntity<LocationResponse> {
        return ResponseEntity.ok(locationService.findById(locationId))
    }

    @PutMapping("/{locationId}")
    fun updateLocation(
        @PathVariable locationId: UUID,
        @Valid @RequestBody request: UpdateLocationRequest,
    ): ResponseEntity<LocationResponse> {
        return ResponseEntity.ok(locationService.updateLocation(locationId, request))
    }

    @DeleteMapping("/{locationId}")
    fun deactivateLocation(@PathVariable locationId: UUID): ResponseEntity<Void> {
        locationService.deactivateLocation(locationId)
        return ResponseEntity.noContent().build()
    }
}
