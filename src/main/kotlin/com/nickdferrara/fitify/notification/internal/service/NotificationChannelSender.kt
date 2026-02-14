package com.nickdferrara.fitify.notification.internal.service

import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import java.util.UUID

internal interface NotificationChannelSender {
    fun send(userId: UUID, subject: String, body: String, payload: Map<String, String> = emptyMap())
    fun supports(channel: NotificationChannel): Boolean
}
