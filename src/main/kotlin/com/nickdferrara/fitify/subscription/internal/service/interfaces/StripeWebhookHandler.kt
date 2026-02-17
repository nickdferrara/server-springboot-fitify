package com.nickdferrara.fitify.subscription.internal.service.interfaces

internal interface StripeWebhookHandler {
    fun handleSubscriptionCreated(stripeSubscriptionId: String, customerId: String, metadata: Map<String, String>)
    fun handleSubscriptionRenewed(stripeSubscriptionId: String, amountPaid: Long, paymentIntentId: String?)
    fun handleSubscriptionExpired(stripeSubscriptionId: String)
    fun handlePaymentFailed(stripeSubscriptionId: String, paymentIntentId: String?)
}
