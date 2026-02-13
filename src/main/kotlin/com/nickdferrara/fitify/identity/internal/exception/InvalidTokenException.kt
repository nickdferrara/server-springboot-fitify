package com.nickdferrara.fitify.identity.internal.exception

internal class InvalidTokenException(reason: String) :
    RuntimeException("Invalid reset token: $reason")
