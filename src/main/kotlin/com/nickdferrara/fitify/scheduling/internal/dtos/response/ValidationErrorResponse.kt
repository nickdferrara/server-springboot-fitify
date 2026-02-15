package com.nickdferrara.fitify.scheduling.internal.dtos.response

internal data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>,
)
