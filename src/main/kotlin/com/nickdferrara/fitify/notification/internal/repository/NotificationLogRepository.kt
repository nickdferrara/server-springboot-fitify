package com.nickdferrara.fitify.notification.internal.repository

import com.nickdferrara.fitify.notification.internal.entities.NotificationLog
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface NotificationLogRepository : JpaRepository<NotificationLog, UUID> {
    fun findByUserId(userId: UUID): List<NotificationLog>
}
