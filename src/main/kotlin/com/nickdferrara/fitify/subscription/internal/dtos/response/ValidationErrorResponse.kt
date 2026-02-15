package com.nickdferrara.fitify.subscription.internal.dtos.response

internal data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>,
)
