package com.nickdferrara.fitify.scheduling.internal.controller

import com.nickdferrara.fitify.scheduling.internal.dtos.response.WaitlistEntryResponse
import com.nickdferrara.fitify.scheduling.internal.service.interfaces.SchedulingService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
internal class WaitlistController(
    private val schedulingService: SchedulingService,
) {

    @GetMapping("/api/v1/users/me/waitlist")
    fun getUserWaitlist(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<WaitlistEntryResponse>> {
        val userId = UUID.fromString(jwt.subject)
        return ResponseEntity.ok(schedulingService.getUserWaitlistEntries(userId))
    }

    @DeleteMapping("/api/v1/classes/{classId}/waitlist")
    fun removeFromWaitlist(
        @PathVariable classId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Void> {
        val userId = UUID.fromString(jwt.subject)
        schedulingService.removeFromWaitlist(classId, userId)
        return ResponseEntity.noContent().build()
    }
}
