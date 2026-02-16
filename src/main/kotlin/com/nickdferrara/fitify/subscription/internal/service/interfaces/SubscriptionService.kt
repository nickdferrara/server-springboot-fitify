package com.nickdferrara.fitify.subscription.internal.service.interfaces

import com.nickdferrara.fitify.subscription.internal.dtos.request.ChangePlanRequest
import com.nickdferrara.fitify.subscription.internal.dtos.request.CheckoutRequest
import com.nickdferrara.fitify.subscription.internal.dtos.response.BillingPortalResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.CheckoutResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionPlanResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionResponse
import java.util.UUID

internal interface SubscriptionService {
    fun getAvailablePlans(): List<SubscriptionPlanResponse>
    fun getCurrentSubscription(userId: UUID): SubscriptionResponse
    fun createCheckoutSession(userId: UUID, request: CheckoutRequest): CheckoutResponse
    fun cancelSubscription(userId: UUID): SubscriptionResponse
    fun changePlan(userId: UUID, request: ChangePlanRequest): CheckoutResponse
    fun createBillingPortalSession(userId: UUID): BillingPortalResponse
    fun handleSubscriptionCreated(stripeSubscriptionId: String, customerId: String, metadata: Map<String, String>)
    fun handleSubscriptionRenewed(stripeSubscriptionId: String, amountPaid: Long, paymentIntentId: String?)
    fun handleSubscriptionExpired(stripeSubscriptionId: String)
    fun handlePaymentFailed(stripeSubscriptionId: String, paymentIntentId: String?)
}
