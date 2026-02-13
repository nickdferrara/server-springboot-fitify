package com.nickdferrara.fitify.location.internal

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
internal class AdminLocationController(
    private val locationService: LocationService,
) {

    @PostMapping
    fun createLocation(@RequestBody request: CreateLocationRequest): ResponseEntity<LocationResponse> {
        val response = locationService.createLocation(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun listLocations(): ResponseEntity<List<LocationResponse>> {
        return ResponseEntity.ok(locationService.findAll())
    }

    @GetMapping("/{locationId}")
    fun getLocation(@PathVariable locationId: UUID): ResponseEntity<LocationResponse> {
        return ResponseEntity.ok(locationService.findById(locationId))
    }

    @PutMapping("/{locationId}")
    fun updateLocation(
        @PathVariable locationId: UUID,
        @RequestBody request: UpdateLocationRequest,
    ): ResponseEntity<LocationResponse> {
        return ResponseEntity.ok(locationService.updateLocation(locationId, request))
    }

    @DeleteMapping("/{locationId}")
    fun deactivateLocation(@PathVariable locationId: UUID): ResponseEntity<Void> {
        locationService.deactivateLocation(locationId)
        return ResponseEntity.noContent().build()
    }
}
