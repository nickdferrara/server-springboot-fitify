package com.nickdferrara.fitify.subscription.internal.gateway

internal interface StripePaymentGateway {

    fun createCheckoutSession(
        stripePriceId: String,
        successUrl: String,
        cancelUrl: String,
        metadata: Map<String, String>,
        couponId: String? = null,
    ): CheckoutSessionResult

    fun retrieveSubscription(stripeSubscriptionId: String): SubscriptionDetails

    fun cancelSubscriptionAtPeriodEnd(stripeSubscriptionId: String)

    fun cancelSubscriptionImmediately(stripeSubscriptionId: String)

    fun createBillingPortalSession(customerId: String, returnUrl: String): String

    fun createCoupon(amountOffCents: Long, currency: String): String

    data class CheckoutSessionResult(
        val sessionId: String,
        val url: String,
    )

    data class SubscriptionDetails(
        val currentPeriodStart: Long,
        val currentPeriodEnd: Long,
        val customer: String,
    )
}
