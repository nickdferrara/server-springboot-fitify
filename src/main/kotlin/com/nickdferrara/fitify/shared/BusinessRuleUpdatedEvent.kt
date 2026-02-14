package com.nickdferrara.fitify.shared

import java.util.UUID

data class BusinessRuleUpdatedEvent(
    val ruleKey: String,
    val newValue: String,
    val locationId: UUID?,
    val updatedBy: String,
)
