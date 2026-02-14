package com.nickdferrara.fitify.notification

import java.util.UUID

interface NotificationApi {
    fun registerDeviceToken(userId: UUID, token: String, deviceType: String)
    fun removeDeviceToken(userId: UUID, token: String)
}
