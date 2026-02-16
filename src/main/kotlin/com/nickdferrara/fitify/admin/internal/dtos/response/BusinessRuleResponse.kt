package com.nickdferrara.fitify.admin.internal.dtos.response

import java.time.Instant
import java.util.UUID

internal data class BusinessRuleResponse(
    val id: UUID,
    val ruleKey: String,
    val value: String,
    val locationId: UUID?,
    val description: String?,
    val updatedBy: String,
    val updatedAt: Instant?,
)
