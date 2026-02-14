package com.nickdferrara.fitify.subscription.internal.dtos.request

import java.util.UUID

internal data class ChangePlanRequest(
    val newPlanId: UUID,
)
