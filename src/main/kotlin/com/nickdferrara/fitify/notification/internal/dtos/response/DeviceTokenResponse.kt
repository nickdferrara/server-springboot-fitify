package com.nickdferrara.fitify.notification.internal.dtos.response

import com.nickdferrara.fitify.notification.internal.entities.DeviceToken
import java.time.Instant
import java.util.UUID

internal data class DeviceTokenResponse(
    val id: UUID,
    val userId: UUID,
    val deviceType: String,
    val lastUsedAt: Instant?,
)

internal fun DeviceToken.toResponse() = DeviceTokenResponse(
    id = id!!,
    userId = userId,
    deviceType = deviceType,
    lastUsedAt = lastUsedAt,
)
