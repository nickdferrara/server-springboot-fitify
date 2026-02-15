package com.nickdferrara.fitify.security.internal.dtos.response

internal data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>,
)
