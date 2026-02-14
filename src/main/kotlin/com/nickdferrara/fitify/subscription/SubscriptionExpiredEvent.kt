package com.nickdferrara.fitify.subscription

import java.util.UUID

data class SubscriptionExpiredEvent(
    val subscriptionId: UUID,
    val userId: UUID,
)
