package com.nickdferrara.fitify.scheduling.internal.controller

import com.nickdferrara.fitify.scheduling.internal.dtos.response.ClassResponse
import com.nickdferrara.fitify.scheduling.internal.model.BookClassResult
import com.nickdferrara.fitify.scheduling.internal.service.SchedulingService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/classes")
internal class ClassController(
    private val schedulingService: SchedulingService,
) {

    @GetMapping
    fun searchClasses(
        @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false) classType: String?,
        @RequestParam(required = false) coachId: UUID?,
        @RequestParam(required = false) locationId: UUID?,
        @RequestParam(required = false) available: Boolean?,
        pageable: Pageable,
    ): ResponseEntity<Page<ClassResponse>> {
        return ResponseEntity.ok(
            schedulingService.searchClasses(date, classType, coachId, locationId, available, pageable)
        )
    }

    @PostMapping("/{classId}/book")
    fun bookClass(
        @PathVariable classId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Any> {
        val userId = UUID.fromString(jwt.subject)
        return when (val result = schedulingService.bookClass(classId, userId)) {
            is BookClassResult.Booked -> ResponseEntity.status(HttpStatus.CREATED).body(result.booking)
            is BookClassResult.Waitlisted -> ResponseEntity.status(HttpStatus.ACCEPTED).body(result.waitlistEntry)
        }
    }

    @DeleteMapping("/{classId}/booking")
    fun cancelBooking(
        @PathVariable classId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Void> {
        val userId = UUID.fromString(jwt.subject)
        schedulingService.cancelBooking(classId, userId)
        return ResponseEntity.noContent().build()
    }
}
