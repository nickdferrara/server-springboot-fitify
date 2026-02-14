package com.nickdferrara.fitify.subscription.internal.lifecycle

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
internal class MonthlySubscriptionLifecycle : SubscriptionLifecycleTemplate() {

    override fun calculateExpiresAt(periodEnd: Instant): Instant {
        return periodEnd.plus(Duration.ofDays(3))
    }
}
