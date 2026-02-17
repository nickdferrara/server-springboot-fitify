package com.nickdferrara.fitify.logging.internal.controller

import com.nickdferrara.fitify.logging.AuditLogResponse
import com.nickdferrara.fitify.logging.internal.service.interfaces.AuditLogService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
internal class AuditLogController(
    private val auditLogService: AuditLogService,
) {

    @GetMapping
    fun queryAuditLogs(
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) module: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
    ): ResponseEntity<List<AuditLogResponse>> {
        val logs = auditLogService.queryAuditLogs(userId, action, module, from, to)
        return ResponseEntity.ok(logs)
    }
}
