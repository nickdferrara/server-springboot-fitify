package com.nickdferrara.fitify.notification.internal.dtos.response

import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.notification.internal.entities.NotificationStatus
import java.time.Instant
import java.util.UUID

internal data class NotificationLogResponse(
    val id: UUID,
    val userId: UUID,
    val channel: NotificationChannel,
    val eventType: String,
    val status: NotificationStatus,
    val sentAt: Instant,
)
