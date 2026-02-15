package com.nickdferrara.fitify.subscription.internal.lifecycle

import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import com.nickdferrara.fitify.subscription.internal.entities.Subscription
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionStateException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

internal class SubscriptionLifecycleTest {

    private val monthlyLifecycle = MonthlySubscriptionLifecycle()
    private val annualLifecycle = AnnualSubscriptionLifecycle()

    private fun buildSubscription(
        status: SubscriptionStatus = SubscriptionStatus.PAST_DUE,
        planType: PlanType = PlanType.MONTHLY,
    ) = Subscription(
        userId = UUID.randomUUID(),
        planType = planType,
        status = status,
    )

    private val periodStart: Instant = Instant.parse("2025-01-01T00:00:00Z")
    private val periodEnd: Instant = Instant.parse("2025-02-01T00:00:00Z")

    @Nested
    inner class MonthlyActivation {

        @Test
        fun `activate sets status to ACTIVE and applies 3-day grace period`() {
            val subscription = buildSubscription(status = SubscriptionStatus.PAST_DUE)

            monthlyLifecycle.activate(subscription, periodStart, periodEnd)

            assertThat(subscription.status).isEqualTo(SubscriptionStatus.ACTIVE)
            assertThat(subscription.currentPeriodStart).isEqualTo(periodStart)
            assertThat(subscription.currentPeriodEnd).isEqualTo(periodEnd)
            assertThat(subscription.expiresAt).isEqualTo(periodEnd.plus(Duration.ofDays(3)))
        }

        @Test
        fun `activate throws when subscription is already active`() {
            val subscription = buildSubscription(status = SubscriptionStatus.ACTIVE)

            assertThatThrownBy { monthlyLifecycle.activate(subscription, periodStart, periodEnd) }
                .isInstanceOf(SubscriptionStateException::class.java)
                .hasMessageContaining("already active")
        }
    }

    @Nested
    inner class MonthlyRenewal {

        @Test
        fun `renew updates period dates and extends grace`() {
            val subscription = buildSubscription(status = SubscriptionStatus.ACTIVE)
            val newStart = Instant.parse("2025-02-01T00:00:00Z")
            val newEnd = Instant.parse("2025-03-01T00:00:00Z")

            monthlyLifecycle.renew(subscription, newStart, newEnd)

            assertThat(subscription.status).isEqualTo(SubscriptionStatus.ACTIVE)
            assertThat(subscription.currentPeriodStart).isEqualTo(newStart)
            assertThat(subscription.currentPeriodEnd).isEqualTo(newEnd)
            assertThat(subscription.expiresAt).isEqualTo(newEnd.plus(Duration.ofDays(3)))
        }

        @Test
        fun `renew throws when subscription is expired`() {
            val subscription = buildSubscription(status = SubscriptionStatus.EXPIRED)

            assertThatThrownBy { monthlyLifecycle.renew(subscription, periodStart, periodEnd) }
                .isInstanceOf(SubscriptionStateException::class.java)
                .hasMessageContaining("expired")
        }
    }

    @Nested
    inner class MonthlyCancellation {

        @Test
        fun `cancel sets status to CANCELLING`() {
            val subscription = buildSubscription(status = SubscriptionStatus.ACTIVE)

            monthlyLifecycle.cancel(subscription)

            assertThat(subscription.status).isEqualTo(SubscriptionStatus.CANCELLING)
        }

        @Test
        fun `cancel throws when subscription is not active`() {
            val subscription = buildSubscription(status = SubscriptionStatus.PAST_DUE)

            assertThatThrownBy { monthlyLifecycle.cancel(subscription) }
                .isInstanceOf(SubscriptionStateException::class.java)
                .hasMessageContaining("active")
        }
    }

    @Nested
    inner class MonthlyExpiration {

        @Test
        fun `expire sets status to EXPIRED`() {
            val subscription = buildSubscription(status = SubscriptionStatus.ACTIVE)

            monthlyLifecycle.expire(subscription)

            assertThat(subscription.status).isEqualTo(SubscriptionStatus.EXPIRED)
        }
    }

    @Nested
    inner class AnnualActivation {

        @Test
        fun `activate sets 7-day grace period for annual plans`() {
            val subscription = buildSubscription(
                status = SubscriptionStatus.PAST_DUE,
                planType = PlanType.ANNUAL,
            )
            val annualEnd = Instant.parse("2026-01-01T00:00:00Z")

            annualLifecycle.activate(subscription, periodStart, annualEnd)

            assertThat(subscription.status).isEqualTo(SubscriptionStatus.ACTIVE)
            assertThat(subscription.expiresAt).isEqualTo(annualEnd.plus(Duration.ofDays(7)))
        }
    }
}
