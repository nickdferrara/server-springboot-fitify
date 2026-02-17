package com.nickdferrara.fitify.subscription.internal.gateway

import com.nickdferrara.fitify.subscription.internal.config.StripeProperties
import com.nickdferrara.fitify.subscription.internal.exception.StripeException
import com.stripe.Stripe
import com.stripe.model.Coupon
import com.stripe.model.Subscription
import com.stripe.model.checkout.Session
import com.stripe.param.CouponCreateParams
import com.stripe.param.SubscriptionUpdateParams
import com.stripe.param.billingportal.SessionCreateParams as PortalSessionCreateParams
import com.stripe.param.checkout.SessionCreateParams
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component

@Component
@EnableConfigurationProperties(StripeProperties::class)
internal class StripePaymentGatewayImpl(
    private val stripeProperties: StripeProperties,
) : StripePaymentGateway {

    @PostConstruct
    fun init() {
        Stripe.apiKey = stripeProperties.secretKey
    }

    override fun createCheckoutSession(
        stripePriceId: String,
        successUrl: String,
        cancelUrl: String,
        metadata: Map<String, String>,
        couponId: String?,
    ): StripePaymentGateway.CheckoutSessionResult {
        try {
            val paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(stripePriceId)
                        .setQuantity(1)
                        .build()
                )

            metadata.forEach { (key, value) -> paramsBuilder.putMetadata(key, value) }

            if (couponId != null) {
                paramsBuilder.addDiscount(
                    SessionCreateParams.Discount.builder()
                        .setCoupon(couponId)
                        .build()
                )
            }

            val session = Session.create(paramsBuilder.build())
            return StripePaymentGateway.CheckoutSessionResult(
                sessionId = session.id,
                url = session.url,
            )
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to create checkout session", e)
        }
    }

    override fun retrieveSubscription(stripeSubscriptionId: String): StripePaymentGateway.SubscriptionDetails {
        try {
            val stripeSub = Subscription.retrieve(stripeSubscriptionId)
            return StripePaymentGateway.SubscriptionDetails(
                currentPeriodStart = stripeSub.currentPeriodStart,
                currentPeriodEnd = stripeSub.currentPeriodEnd,
                customer = stripeSub.customer,
            )
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to retrieve subscription from Stripe", e)
        }
    }

    override fun cancelSubscriptionAtPeriodEnd(stripeSubscriptionId: String) {
        try {
            val stripeSub = Subscription.retrieve(stripeSubscriptionId)
            val params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build()
            stripeSub.update(params)
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to cancel subscription on Stripe", e)
        }
    }

    override fun cancelSubscriptionImmediately(stripeSubscriptionId: String) {
        try {
            val stripeSub = Subscription.retrieve(stripeSubscriptionId)
            stripeSub.cancel()
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to cancel existing subscription on Stripe", e)
        }
    }

    override fun createBillingPortalSession(customerId: String, returnUrl: String): String {
        try {
            val params = PortalSessionCreateParams.builder()
                .setCustomer(customerId)
                .setReturnUrl(returnUrl)
                .build()
            val portalSession = com.stripe.model.billingportal.Session.create(params)
            return portalSession.url
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to create billing portal session", e)
        }
    }

    override fun createCoupon(amountOffCents: Long, currency: String): String {
        try {
            val params = CouponCreateParams.builder()
                .setAmountOff(amountOffCents)
                .setCurrency(currency)
                .setDuration(CouponCreateParams.Duration.ONCE)
                .build()
            val coupon = Coupon.create(params)
            return coupon.id
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to create coupon", e)
        }
    }
}
