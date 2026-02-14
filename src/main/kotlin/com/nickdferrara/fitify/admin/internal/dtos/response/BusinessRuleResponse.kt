package com.nickdferrara.fitify.admin.internal.dtos.response

import com.nickdferrara.fitify.admin.internal.entities.BusinessRule
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

internal fun BusinessRule.toResponse() = BusinessRuleResponse(
    id = id!!,
    ruleKey = ruleKey,
    value = value,
    locationId = locationId,
    description = description,
    updatedBy = updatedBy,
    updatedAt = updatedAt,
)
