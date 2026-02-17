package com.nickdferrara.fitify.subscription.internal.controller

import com.nickdferrara.fitify.subscription.internal.config.StripeProperties
import com.nickdferrara.fitify.subscription.internal.exception.InvalidWebhookSignatureException
import com.nickdferrara.fitify.subscription.internal.service.interfaces.StripeWebhookHandler
import com.stripe.model.Event
import com.stripe.model.Invoice
import com.stripe.net.Webhook
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/webhooks")
internal class StripeWebhookController(
    private val stripeWebhookHandler: StripeWebhookHandler,
    private val stripeProperties: StripeProperties,
) {

    private val logger = LoggerFactory.getLogger(StripeWebhookController::class.java)

    @PostMapping("/stripe")
    fun handleStripeWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") sigHeader: String,
    ): ResponseEntity<Void> {
        val event: Event = try {
            Webhook.constructEvent(payload, sigHeader, stripeProperties.webhookSecret)
        } catch (e: Exception) {
            throw InvalidWebhookSignatureException("Invalid Stripe webhook signature")
        }

        logger.debug("Received Stripe event: {}", event.type)

        when (event.type) {
            "customer.subscription.created" -> {
                val stripeObject = event.dataObjectDeserializer.`object`.orElse(null)
                if (stripeObject is com.stripe.model.Subscription) {
                    stripeWebhookHandler.handleSubscriptionCreated(
                        stripeSubscriptionId = stripeObject.id,
                        customerId = stripeObject.customer,
                        metadata = stripeObject.metadata ?: emptyMap(),
                    )
                }
            }
            "invoice.paid" -> {
                val stripeObject = event.dataObjectDeserializer.`object`.orElse(null)
                if (stripeObject is Invoice) {
                    val subscriptionId = stripeObject.subscription
                    if (subscriptionId != null) {
                        stripeWebhookHandler.handleSubscriptionRenewed(
                            stripeSubscriptionId = subscriptionId,
                            amountPaid = stripeObject.amountPaid,
                            paymentIntentId = stripeObject.paymentIntent,
                        )
                    }
                }
            }
            "invoice.payment_failed" -> {
                val stripeObject = event.dataObjectDeserializer.`object`.orElse(null)
                if (stripeObject is Invoice) {
                    val subscriptionId = stripeObject.subscription
                    if (subscriptionId != null) {
                        stripeWebhookHandler.handlePaymentFailed(
                            stripeSubscriptionId = subscriptionId,
                            paymentIntentId = stripeObject.paymentIntent,
                        )
                    }
                }
            }
            "customer.subscription.deleted" -> {
                val stripeObject = event.dataObjectDeserializer.`object`.orElse(null)
                if (stripeObject is com.stripe.model.Subscription) {
                    stripeWebhookHandler.handleSubscriptionExpired(stripeObject.id)
                }
            }
            "customer.subscription.updated" -> {
                val stripeObject = event.dataObjectDeserializer.`object`.orElse(null)
                if (stripeObject is com.stripe.model.Subscription) {
                    if (stripeObject.status == "active") {
                        stripeWebhookHandler.handleSubscriptionRenewed(
                            stripeSubscriptionId = stripeObject.id,
                            amountPaid = 0,
                            paymentIntentId = null,
                        )
                    }
                }
            }
            else -> logger.debug("Unhandled Stripe event type: {}", event.type)
        }

        return ResponseEntity.ok().build()
    }
}
