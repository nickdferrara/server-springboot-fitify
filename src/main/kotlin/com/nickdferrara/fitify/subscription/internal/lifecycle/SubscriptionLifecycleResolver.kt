package com.nickdferrara.fitify.subscription.internal.lifecycle

import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import org.springframework.stereotype.Component

@Component
internal class SubscriptionLifecycleResolver(
    private val monthlyLifecycle: MonthlySubscriptionLifecycle,
    private val annualLifecycle: AnnualSubscriptionLifecycle,
) {

    fun resolve(planType: PlanType): SubscriptionLifecycleTemplate {
        return when (planType) {
            PlanType.MONTHLY -> monthlyLifecycle
            PlanType.ANNUAL -> annualLifecycle
        }
    }
}
