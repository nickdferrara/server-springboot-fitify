package com.nickdferrara.fitify.subscription.internal.repository

import com.nickdferrara.fitify.subscription.internal.entities.Subscription
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

internal interface SubscriptionRepository : JpaRepository<Subscription, UUID> {
    fun findByUserId(userId: UUID): List<Subscription>
    fun findByStripeSubscriptionId(stripeSubscriptionId: String): Subscription?
    fun findByUserIdAndStatusIn(userId: UUID, statuses: List<SubscriptionStatus>): Subscription?
    fun existsByUserIdAndStatusIn(userId: UUID, statuses: List<SubscriptionStatus>): Boolean

    @Query("SELECT s.planType, COUNT(s) FROM Subscription s WHERE s.status IN :statuses GROUP BY s.planType")
    fun countByStatusInGroupByPlanType(statuses: List<SubscriptionStatus>): List<Array<Any>>

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'EXPIRED' AND s.currentPeriodEnd BETWEEN :start AND :end")
    fun countExpiredBetween(start: Instant, end: Instant): Long

    @Query(
        """
        SELECT COUNT(s) FROM Subscription s
        WHERE s.createdAt <= :asOf
          AND (s.status IN ('ACTIVE', 'CANCELLING') OR s.currentPeriodEnd >= :asOf)
        """
    )
    fun countActiveAsOf(asOf: Instant): Long
}
