package com.nickdferrara.fitify.location.internal

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/locations")
internal class LocationController(
    private val locationService: LocationService,
) {

    @GetMapping
    fun listActiveLocations(): ResponseEntity<List<LocationResponse>> {
        return ResponseEntity.ok(locationService.findAllActive())
    }
}
