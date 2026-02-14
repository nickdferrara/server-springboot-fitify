package com.nickdferrara.fitify.notification

import java.util.UUID

data class NotificationFailedEvent(
    val notificationId: UUID,
    val channel: String,
    val errorMessage: String,
)
