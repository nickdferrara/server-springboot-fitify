package com.nickdferrara.fitify.subscription.internal.repository

import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionPlan
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface SubscriptionPlanRepository : JpaRepository<SubscriptionPlan, UUID> {
    fun findByActiveTrue(): List<SubscriptionPlan>
    fun findByPlanTypeAndActiveTrue(planType: PlanType): SubscriptionPlan?
}
