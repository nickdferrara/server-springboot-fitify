package com.nickdferrara.fitify.notification.internal.dtos.request

internal data class RegisterDeviceTokenRequest(
    val token: String,
    val deviceType: String,
)
