package com.nickdferrara.fitify.admin.internal.controller

import com.nickdferrara.fitify.admin.internal.AdminService
import com.nickdferrara.fitify.admin.internal.dtos.request.CreateClassRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.CreateRecurringScheduleRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.UpdateClassRequest
import com.nickdferrara.fitify.admin.internal.dtos.response.AdminClassResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.CancelClassResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.RecurringScheduleResponse
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
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
internal class AdminClassController(
    private val adminService: AdminService,
) {

    @PostMapping("/locations/{locationId}/classes")
    fun createClass(
        @PathVariable locationId: UUID,
        @RequestBody request: CreateClassRequest,
    ): ResponseEntity<AdminClassResponse> {
        val response = adminService.createClass(locationId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/locations/{locationId}/classes")
    fun listClassesByLocation(
        @PathVariable locationId: UUID,
    ): ResponseEntity<List<AdminClassResponse>> {
        return ResponseEntity.ok(adminService.listClassesByLocation(locationId))
    }

    @PutMapping("/classes/{classId}")
    fun updateClass(
        @PathVariable classId: UUID,
        @RequestBody request: UpdateClassRequest,
    ): ResponseEntity<AdminClassResponse> {
        return ResponseEntity.ok(adminService.updateClass(classId, request))
    }

    @DeleteMapping("/classes/{classId}")
    fun cancelClass(@PathVariable classId: UUID): ResponseEntity<CancelClassResponse> {
        val response = adminService.cancelClass(classId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/locations/{locationId}/classes/recurring")
    fun createRecurringSchedule(
        @PathVariable locationId: UUID,
        @RequestBody request: CreateRecurringScheduleRequest,
    ): ResponseEntity<RecurringScheduleResponse> {
        val response = adminService.createRecurringSchedule(locationId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
