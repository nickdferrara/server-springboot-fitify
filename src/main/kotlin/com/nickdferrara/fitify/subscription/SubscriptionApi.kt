package com.nickdferrara.fitify.subscription

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class SubscriptionSummary(
    val id: UUID,
    val userId: UUID,
    val planType: String,
    val status: String,
    val currentPeriodEnd: Instant?,
)

interface SubscriptionApi {
    fun findActiveSubscriptionByUserId(userId: UUID): SubscriptionSummary?
    fun hasActiveSubscription(userId: UUID): Boolean
    fun countActiveSubscriptionsByPlanType(): Map<String, Long>
    fun countSubscriptionsExpiredBetween(start: Instant, end: Instant): Long
    fun countActiveSubscriptionsAsOf(asOf: Instant): Long
    fun sumRevenueBetween(start: Instant, end: Instant): BigDecimal
    fun sumRevenueBetweenByPlanType(start: Instant, end: Instant): Map<String, BigDecimal>
}
