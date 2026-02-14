package com.nickdferrara.fitify.subscription.internal.repository

import com.nickdferrara.fitify.subscription.internal.entities.PaymentHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface PaymentHistoryRepository : JpaRepository<PaymentHistory, UUID> {
    fun findBySubscriptionId(subscriptionId: UUID): List<PaymentHistory>
    fun findByUserId(userId: UUID): List<PaymentHistory>
}
