package com.nickdferrara.fitify.identity.internal.exception

internal class EmailAlreadyExistsException(email: String) :
    RuntimeException("Email already registered: $email")
