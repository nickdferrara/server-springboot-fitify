package com.nickdferrara.fitify.logging.internal.repository

import com.nickdferrara.fitify.logging.internal.entities.AuditLogEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

internal interface AuditLogRepository : JpaRepository<AuditLogEntity, UUID> {

    @Query(
        """
        SELECT a FROM AuditLogEntity a
        WHERE (:userId IS NULL OR a.userId = :userId)
        AND (:action IS NULL OR a.action = :action)
        AND (:module IS NULL OR a.module = :module)
        AND (:from IS NULL OR a.timestamp >= :from)
        AND (:to IS NULL OR a.timestamp <= :to)
        ORDER BY a.timestamp DESC
        """,
    )
    fun findByFilters(
        userId: String?,
        action: String?,
        module: String?,
        from: Instant?,
        to: Instant?,
        pageable: Pageable,
    ): Page<AuditLogEntity>
}
