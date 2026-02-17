package com.nickdferrara.fitify.coaching.internal.controller

import com.nickdferrara.fitify.coaching.internal.dtos.request.AssignCoachLocationsRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.CreateCoachRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.UpdateCoachRequest
import com.nickdferrara.fitify.coaching.internal.dtos.response.CoachResponse
import com.nickdferrara.fitify.coaching.internal.service.interfaces.CoachingService
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
@RequestMapping("/api/v1/admin/coaches")
@PreAuthorize("hasRole('ADMIN')")
internal class AdminCoachController(
    private val coachingService: CoachingService,
) {

    @PostMapping
    fun createCoach(@Valid @RequestBody request: CreateCoachRequest): ResponseEntity<CoachResponse> {
        val response = coachingService.createCoach(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun listCoaches(): ResponseEntity<List<CoachResponse>> {
        return ResponseEntity.ok(coachingService.findAll())
    }

    @GetMapping("/{coachId}")
    fun getCoach(@PathVariable coachId: UUID): ResponseEntity<CoachResponse> {
        return ResponseEntity.ok(coachingService.findById(coachId))
    }

    @PutMapping("/{coachId}")
    fun updateCoach(
        @PathVariable coachId: UUID,
        @Valid @RequestBody request: UpdateCoachRequest,
    ): ResponseEntity<CoachResponse> {
        return ResponseEntity.ok(coachingService.updateCoach(coachId, request))
    }

    @DeleteMapping("/{coachId}")
    fun deactivateCoach(@PathVariable coachId: UUID): ResponseEntity<Void> {
        coachingService.deactivateCoach(coachId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{coachId}/locations")
    fun assignLocations(
        @PathVariable coachId: UUID,
        @Valid @RequestBody request: AssignCoachLocationsRequest,
    ): ResponseEntity<CoachResponse> {
        return ResponseEntity.ok(coachingService.assignLocations(coachId, request))
    }
}
