package com.nickdferrara.fitify.notification.internal.exception

internal class DeviceTokenNotFoundException(token: String) :
    RuntimeException("Device token not found: $token")
