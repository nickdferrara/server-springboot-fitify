package com.nickdferrara.fitify.notification.internal.extensions

import com.nickdferrara.fitify.notification.internal.dtos.response.DeviceTokenResponse
import com.nickdferrara.fitify.notification.internal.entities.DeviceToken

internal fun DeviceToken.toResponse() = DeviceTokenResponse(
    id = id!!,
    userId = userId,
    deviceType = deviceType,
    lastUsedAt = lastUsedAt,
)
