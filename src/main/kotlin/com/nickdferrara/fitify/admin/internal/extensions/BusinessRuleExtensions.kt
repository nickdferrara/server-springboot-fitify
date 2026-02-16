package com.nickdferrara.fitify.admin.internal.extensions

import com.nickdferrara.fitify.admin.internal.dtos.response.BusinessRuleResponse
import com.nickdferrara.fitify.admin.internal.entities.BusinessRule

internal fun BusinessRule.toResponse() = BusinessRuleResponse(
    id = id!!,
    ruleKey = ruleKey,
    value = value,
    locationId = locationId,
    description = description,
    updatedBy = updatedBy,
    updatedAt = updatedAt,
)
