package com.nickdferrara.fitify.subscription.internal.controller

import com.nickdferrara.fitify.subscription.internal.config.StripeProperties
import com.nickdferrara.fitify.subscription.internal.exception.InvalidWebhookSignatureException
import com.nickdferrara.fitify.subscription.internal.service.SubscriptionService
import com.stripe.model.Event
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.Invoice
import com.stripe.model.StripeObject
import com.stripe.net.Webhook
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

internal class StripeWebhookControllerTest {

    private val subscriptionService = mockk<SubscriptionService>(relaxed = true)
    private val stripeProperties = StripeProperties(
        secretKey = "sk_test",
        webhookSecret = "whsec_test",
        successUrl = "http://localhost/success",
        cancelUrl = "http://localhost/cancel",
    )
    private val controller = StripeWebhookController(subscriptionService, stripeProperties)

    @BeforeEach
    fun setUp() {
        mockkStatic(Webhook::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Webhook::class)
    }

    private fun mockEvent(type: String, stripeObject: StripeObject?): Event {
        val deserializer = mockk<EventDataObjectDeserializer>()
        every { deserializer.`object` } returns if (stripeObject != null) Optional.of(stripeObject) else Optional.empty()
        val event = mockk<Event>()
        every { event.type } returns type
        every { event.dataObjectDeserializer } returns deserializer
        return event
    }

    private fun stubValidSignature(event: Event) {
        every { Webhook.constructEvent(any(), any(), any()) } returns event
    }

    @Test
    fun `handles subscription created event`() {
        val stripeSubscription = mockk<com.stripe.model.Subscription>()
        every { stripeSubscription.id } returns "sub_123"
        every { stripeSubscription.customer } returns "cus_456"
        every { stripeSubscription.metadata } returns mapOf("userId" to "user-1")

        val event = mockEvent("customer.subscription.created", stripeSubscription)
        stubValidSignature(event)

        val response = controller.handleStripeWebhook("payload", "sig")

        assertThat(response.statusCode.value()).isEqualTo(200)
        verify {
            subscriptionService.handleSubscriptionCreated(
                stripeSubscriptionId = "sub_123",
                customerId = "cus_456",
                metadata = mapOf("userId" to "user-1"),
            )
        }
    }

    @Test
    fun `handles invoice paid event`() {
        val invoice = mockk<Invoice>()
        every { invoice.subscription } returns "sub_123"
        every { invoice.amountPaid } returns 4999L
        every { invoice.paymentIntent } returns "pi_789"

        val event = mockEvent("invoice.paid", invoice)
        stubValidSignature(event)

        val response = controller.handleStripeWebhook("payload", "sig")

        assertThat(response.statusCode.value()).isEqualTo(200)
        verify {
            subscriptionService.handleSubscriptionRenewed(
                stripeSubscriptionId = "sub_123",
                amountPaid = 4999L,
                paymentIntentId = "pi_789",
            )
        }
    }

    @Test
    fun `handles invoice payment failed event`() {
        val invoice = mockk<Invoice>()
        every { invoice.subscription } returns "sub_123"
        every { invoice.paymentIntent } returns "pi_789"

        val event = mockEvent("invoice.payment_failed", invoice)
        stubValidSignature(event)

        val response = controller.handleStripeWebhook("payload", "sig")

        assertThat(response.statusCode.value()).isEqualTo(200)
        verify {
            subscriptionService.handlePaymentFailed(
                stripeSubscriptionId = "sub_123",
                paymentIntentId = "pi_789",
            )
        }
    }

    @Test
    fun `handles subscription deleted event`() {
        val stripeSubscription = mockk<com.stripe.model.Subscription>()
        every { stripeSubscription.id } returns "sub_123"

        val event = mockEvent("customer.subscription.deleted", stripeSubscription)
        stubValidSignature(event)

        val response = controller.handleStripeWebhook("payload", "sig")

        assertThat(response.statusCode.value()).isEqualTo(200)
        verify { subscriptionService.handleSubscriptionExpired("sub_123") }
    }

    @Test
    fun `handles subscription updated event when active`() {
        val stripeSubscription = mockk<com.stripe.model.Subscription>()
        every { stripeSubscription.id } returns "sub_123"
        every { stripeSubscription.status } returns "active"

        val event = mockEvent("customer.subscription.updated", stripeSubscription)
        stubValidSignature(event)

        val response = controller.handleStripeWebhook("payload", "sig")

        assertThat(response.statusCode.value()).isEqualTo(200)
        verify {
            subscriptionService.handleSubscriptionRenewed(
                stripeSubscriptionId = "sub_123",
                amountPaid = 0,
                paymentIntentId = null,
            )
        }
    }

    @Test
    fun `throws InvalidWebhookSignatureException on invalid signature`() {
        every { Webhook.constructEvent(any(), any(), any()) } throws Exception("Invalid signature")

        assertThatThrownBy { controller.handleStripeWebhook("payload", "bad-sig") }
            .isInstanceOf(InvalidWebhookSignatureException::class.java)
    }

    @Test
    fun `unhandled event type returns 200`() {
        val event = mockEvent("unknown.event.type", null)
        stubValidSignature(event)

        val response = controller.handleStripeWebhook("payload", "sig")

        assertThat(response.statusCode.value()).isEqualTo(200)
    }
}
