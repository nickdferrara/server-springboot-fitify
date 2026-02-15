package com.nickdferrara.fitify.logging

import java.time.Instant
import java.util.UUID

data class AuditLogResponse(
    val id: UUID,
    val timestamp: Instant,
    val userId: String,
    val action: String,
    val module: String,
    val resourceType: String,
    val resourceId: String?,
    val details: String?,
    val ipAddress: String?,
)

interface LoggingApi {
    fun queryAuditLogs(
        userId: String? = null,
        action: String? = null,
        module: String? = null,
        from: Instant? = null,
        to: Instant? = null,
    ): List<AuditLogResponse>
}
