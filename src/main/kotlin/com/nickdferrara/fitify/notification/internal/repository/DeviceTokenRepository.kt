package com.nickdferrara.fitify.notification.internal.repository

import com.nickdferrara.fitify.notification.internal.entities.DeviceToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface DeviceTokenRepository : JpaRepository<DeviceToken, UUID> {
    fun findByUserId(userId: UUID): List<DeviceToken>
    fun findByFcmToken(token: String): DeviceToken?
    fun deleteByFcmToken(token: String)
}
