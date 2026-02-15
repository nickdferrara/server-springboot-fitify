package com.nickdferrara.fitify.notification.internal.dtos.response

internal data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>,
)
