package com.nickdferrara.fitify.logging.internal.service

import com.nickdferrara.fitify.logging.AuditLogResponse
import com.nickdferrara.fitify.logging.LoggingApi
import com.nickdferrara.fitify.logging.internal.entities.AuditLogEntity
import com.nickdferrara.fitify.logging.internal.repository.AuditLogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
internal class AuditLogService(
    private val auditLogRepository: AuditLogRepository,
) : LoggingApi {

    @Transactional
    fun save(
        userId: String,
        action: String,
        module: String,
        resourceType: String,
        resourceId: String?,
        details: String?,
        ipAddress: String?,
    ) {
        auditLogRepository.save(
            AuditLogEntity(
                userId = userId,
                action = action,
                module = module,
                resourceType = resourceType,
                resourceId = resourceId,
                details = details,
                ipAddress = ipAddress,
            ),
        )
    }

    override fun queryAuditLogs(
        userId: String?,
        action: String?,
        module: String?,
        from: Instant?,
        to: Instant?,
    ): List<AuditLogResponse> {
        return auditLogRepository.findByFilters(
            userId = userId,
            action = action,
            module = module,
            from = from,
            to = to,
            pageable = PageRequest.of(0, 100),
        ).content.map { it.toResponse() }
    }

    private fun AuditLogEntity.toResponse() = AuditLogResponse(
        id = id!!,
        timestamp = timestamp!!,
        userId = userId,
        action = action,
        module = module,
        resourceType = resourceType,
        resourceId = resourceId,
        details = details,
        ipAddress = ipAddress,
    )
}
