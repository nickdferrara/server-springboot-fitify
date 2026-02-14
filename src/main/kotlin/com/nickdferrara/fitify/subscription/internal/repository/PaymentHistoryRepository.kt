package com.nickdferrara.fitify.subscription.internal.repository

import com.nickdferrara.fitify.subscription.internal.entities.PaymentHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

internal interface PaymentHistoryRepository : JpaRepository<PaymentHistory, UUID> {
    fun findBySubscriptionId(subscriptionId: UUID): List<PaymentHistory>
    fun findByUserId(userId: UUID): List<PaymentHistory>

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentHistory p WHERE p.status = 'succeeded' AND p.createdAt BETWEEN :start AND :end")
    fun sumRevenueBetween(start: Instant, end: Instant): BigDecimal

    @Query(
        """
        SELECT p.subscription.planType, COALESCE(SUM(p.amount), 0)
        FROM PaymentHistory p
        WHERE p.status = 'succeeded' AND p.createdAt BETWEEN :start AND :end
        GROUP BY p.subscription.planType
        """
    )
    fun sumRevenueBetweenGroupByPlanType(start: Instant, end: Instant): List<Array<Any>>
}
