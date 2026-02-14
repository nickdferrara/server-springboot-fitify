package com.nickdferrara.fitify.subscription.internal.repository

import com.nickdferrara.fitify.subscription.internal.entities.Subscription
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface SubscriptionRepository : JpaRepository<Subscription, UUID> {
    fun findByUserId(userId: UUID): List<Subscription>
    fun findByStripeSubscriptionId(stripeSubscriptionId: String): Subscription?
    fun findByUserIdAndStatusIn(userId: UUID, statuses: List<SubscriptionStatus>): Subscription?
    fun existsByUserIdAndStatusIn(userId: UUID, statuses: List<SubscriptionStatus>): Boolean
}
