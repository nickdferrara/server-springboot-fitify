package com.nickdferrara.fitify.identity.internal.exception

import java.util.UUID

internal class UserNotFoundException(identifier: Any) :
    RuntimeException("User not found: $identifier")
