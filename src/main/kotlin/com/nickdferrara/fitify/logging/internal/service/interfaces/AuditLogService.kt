package com.nickdferrara.fitify.logging.internal.service.interfaces

import com.nickdferrara.fitify.logging.AuditLogResponse
import java.time.Instant

internal interface AuditLogService {
    fun save(
        userId: String,
        action: String,
        module: String,
        resourceType: String,
        resourceId: String?,
        details: String?,
        ipAddress: String?,
    )

    fun queryAuditLogs(
        userId: String?,
        action: String?,
        module: String?,
        from: Instant?,
        to: Instant?,
    ): List<AuditLogResponse>
}
