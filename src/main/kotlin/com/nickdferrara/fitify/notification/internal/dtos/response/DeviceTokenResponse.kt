package com.nickdferrara.fitify.notification.internal.dtos.response

import java.time.Instant
import java.util.UUID

internal data class DeviceTokenResponse(
    val id: UUID,
    val userId: UUID,
    val deviceType: String,
    val lastUsedAt: Instant?,
)
