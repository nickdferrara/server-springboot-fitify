package com.nickdferrara.fitify.notification.internal.service.interfaces

import com.nickdferrara.fitify.notification.internal.entities.NotificationLog
import com.nickdferrara.fitify.notification.internal.factory.NotificationPayload
import java.util.UUID

internal interface NotificationService {
    fun sendNotification(userId: UUID, eventType: String, payload: NotificationPayload)
    fun registerDeviceToken(userId: UUID, token: String, deviceType: String)
    fun removeDeviceToken(userId: UUID, token: String)
    fun getNotificationHistory(userId: UUID): List<NotificationLog>
}
