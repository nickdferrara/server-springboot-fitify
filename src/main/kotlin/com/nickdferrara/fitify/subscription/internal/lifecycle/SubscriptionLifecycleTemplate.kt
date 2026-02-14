package com.nickdferrara.fitify.subscription.internal.lifecycle

import com.nickdferrara.fitify.subscription.internal.entities.Subscription
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionStateException
import java.time.Instant

internal abstract class SubscriptionLifecycleTemplate {

    fun activate(subscription: Subscription, periodStart: Instant, periodEnd: Instant): Subscription {
        validateActivation(subscription)
        subscription.status = SubscriptionStatus.ACTIVE
        subscription.currentPeriodStart = periodStart
        subscription.currentPeriodEnd = periodEnd
        subscription.expiresAt = calculateExpiresAt(periodEnd)
        onActivated(subscription)
        return subscription
    }

    fun renew(subscription: Subscription, newPeriodStart: Instant, newPeriodEnd: Instant): Subscription {
        validateRenewal(subscription)
        subscription.status = SubscriptionStatus.ACTIVE
        subscription.currentPeriodStart = newPeriodStart
        subscription.currentPeriodEnd = newPeriodEnd
        subscription.expiresAt = calculateExpiresAt(newPeriodEnd)
        onRenewed(subscription)
        return subscription
    }

    fun cancel(subscription: Subscription): Subscription {
        validateCancellation(subscription)
        subscription.status = SubscriptionStatus.CANCELLING
        onCancelled(subscription)
        return subscription
    }

    fun expire(subscription: Subscription): Subscription {
        subscription.status = SubscriptionStatus.EXPIRED
        onExpired(subscription)
        return subscription
    }

    protected abstract fun calculateExpiresAt(periodEnd: Instant): Instant

    protected open fun validateActivation(subscription: Subscription) {
        if (subscription.status == SubscriptionStatus.ACTIVE) {
            throw SubscriptionStateException("Subscription is already active")
        }
    }

    protected open fun validateRenewal(subscription: Subscription) {
        if (subscription.status == SubscriptionStatus.EXPIRED) {
            throw SubscriptionStateException("Cannot renew an expired subscription")
        }
    }

    protected open fun validateCancellation(subscription: Subscription) {
        if (subscription.status != SubscriptionStatus.ACTIVE) {
            throw SubscriptionStateException("Only active subscriptions can be cancelled")
        }
    }

    protected open fun onActivated(subscription: Subscription) {}
    protected open fun onRenewed(subscription: Subscription) {}
    protected open fun onCancelled(subscription: Subscription) {}
    protected open fun onExpired(subscription: Subscription) {}
}
