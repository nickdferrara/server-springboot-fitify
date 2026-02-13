package com.nickdferrara.fitify.scheduling.internal.controller

import com.nickdferrara.fitify.scheduling.internal.dtos.request.CreateClassRequest
import com.nickdferrara.fitify.scheduling.internal.dtos.request.UpdateClassRequest
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ClassResponse
import com.nickdferrara.fitify.scheduling.internal.service.SchedulingService
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
@RequestMapping("/api/v1/admin")
internal class AdminClassController(
    private val schedulingService: SchedulingService,
) {

    @PostMapping("/locations/{locationId}/classes")
    fun createClass(
        @PathVariable locationId: UUID,
        @RequestBody request: CreateClassRequest,
    ): ResponseEntity<ClassResponse> {
        val response = schedulingService.createClass(locationId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/locations/{locationId}/classes")
    fun listClassesByLocation(
        @PathVariable locationId: UUID,
    ): ResponseEntity<List<ClassResponse>> {
        return ResponseEntity.ok(schedulingService.findClassesByLocationId(locationId))
    }

    @GetMapping("/classes/{classId}")
    fun getClass(@PathVariable classId: UUID): ResponseEntity<ClassResponse> {
        return ResponseEntity.ok(schedulingService.getClass(classId))
    }

    @PutMapping("/classes/{classId}")
    fun updateClass(
        @PathVariable classId: UUID,
        @RequestBody request: UpdateClassRequest,
    ): ResponseEntity<ClassResponse> {
        return ResponseEntity.ok(schedulingService.updateClass(classId, request))
    }

    @DeleteMapping("/classes/{classId}")
    fun cancelClass(@PathVariable classId: UUID): ResponseEntity<Void> {
        schedulingService.cancelClass(classId)
        return ResponseEntity.noContent().build()
    }
}
