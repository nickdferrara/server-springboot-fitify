package com.nickdferrara.fitify.identity.internal.exception

internal class WeakPasswordException :
    RuntimeException("Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character")
