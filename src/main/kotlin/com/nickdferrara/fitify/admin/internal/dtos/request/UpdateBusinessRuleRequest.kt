package com.nickdferrara.fitify.admin.internal.dtos.request

import java.util.UUID

internal data class UpdateBusinessRuleRequest(
    val value: String,
    val description: String? = null,
    val locationId: UUID? = null,
)
