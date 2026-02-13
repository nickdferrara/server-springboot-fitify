package com.nickdferrara.fitify.coaching.internal.controller

import com.nickdferrara.fitify.coaching.internal.dtos.response.CoachResponse
import com.nickdferrara.fitify.coaching.internal.service.CoachingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/locations/{locationId}/coaches")
internal class AdminLocationCoachController(
    private val coachingService: CoachingService,
) {

    @GetMapping
    fun listCoachesByLocation(@PathVariable locationId: UUID): ResponseEntity<List<CoachResponse>> {
        return ResponseEntity.ok(coachingService.findCoachesByLocationId(locationId))
    }
}
