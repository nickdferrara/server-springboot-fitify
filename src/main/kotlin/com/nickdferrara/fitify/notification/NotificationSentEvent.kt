package com.nickdferrara.fitify.notification

import java.util.UUID

data class NotificationSentEvent(
    val notificationId: UUID,
    val channel: String,
    val status: String,
)
