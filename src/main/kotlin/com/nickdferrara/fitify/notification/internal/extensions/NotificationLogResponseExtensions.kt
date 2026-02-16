package com.nickdferrara.fitify.notification.internal.extensions

import com.nickdferrara.fitify.notification.internal.dtos.response.NotificationLogResponse
import com.nickdferrara.fitify.notification.internal.entities.NotificationLog

internal fun NotificationLog.toResponse() = NotificationLogResponse(
    id = id!!,
    userId = userId,
    channel = channel,
    eventType = eventType,
    status = status,
    sentAt = sentAt!!,
)
