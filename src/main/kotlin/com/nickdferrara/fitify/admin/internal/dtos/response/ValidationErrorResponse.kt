package com.nickdferrara.fitify.admin.internal.dtos.response

internal data class ValidationErrorResponse(
    val message: String,
    val errors: Map<String, String>,
)
