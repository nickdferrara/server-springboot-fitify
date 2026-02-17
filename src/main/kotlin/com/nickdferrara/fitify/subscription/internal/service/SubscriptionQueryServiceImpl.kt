package com.nickdferrara.fitify.subscription.internal.service

import com.nickdferrara.fitify.subscription.SubscriptionApi
import com.nickdferrara.fitify.subscription.SubscriptionSummary
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import com.nickdferrara.fitify.subscription.internal.extensions.toSummary
import com.nickdferrara.fitify.subscription.internal.repository.PaymentHistoryRepository
import com.nickdferrara.fitify.subscription.internal.repository.SubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
internal class SubscriptionQueryServiceImpl(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentHistoryRepository: PaymentHistoryRepository,
) : SubscriptionApi {

    override fun findActiveSubscriptionByUserId(userId: UUID): SubscriptionSummary? {
        val subscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        ) ?: return null
        return subscription.toSummary()
    }

    override fun hasActiveSubscription(userId: UUID): Boolean {
        return subscriptionRepository.existsByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        )
    }

    override fun countActiveSubscriptionsByPlanType(): Map<String, Long> {
        return subscriptionRepository
            .countByStatusInGroupByPlanType(listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING))
            .associate { (it[0] as PlanType).name to it[1] as Long }
    }

    override fun countSubscriptionsExpiredBetween(start: Instant, end: Instant): Long {
        return subscriptionRepository.countExpiredBetween(start, end)
    }

    override fun countActiveSubscriptionsAsOf(asOf: Instant): Long {
        return subscriptionRepository.countActiveAsOf(asOf)
    }

    override fun sumRevenueBetween(start: Instant, end: Instant): BigDecimal {
        return paymentHistoryRepository.sumRevenueBetween(start, end)
    }

    override fun sumRevenueBetweenByPlanType(start: Instant, end: Instant): Map<String, BigDecimal> {
        return paymentHistoryRepository
            .sumRevenueBetweenGroupByPlanType(start, end)
            .associate { (it[0] as PlanType).name to it[1] as BigDecimal }
    }
}
