package com.nickdferrara.fitify.subscription.internal.dtos.request

import java.util.UUID

internal data class CheckoutRequest(
    val planId: UUID,
    val promotionalCode: String? = null,
)
