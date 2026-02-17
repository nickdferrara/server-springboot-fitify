package com.nickdferrara.fitify.subscription.internal.service

import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import com.nickdferrara.fitify.subscription.SubscriptionRenewedEvent
import com.nickdferrara.fitify.subscription.internal.entities.PaymentHistory
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import com.nickdferrara.fitify.subscription.internal.entities.Subscription
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import com.nickdferrara.fitify.subscription.internal.gateway.StripePaymentGateway
import com.nickdferrara.fitify.subscription.internal.lifecycle.SubscriptionLifecycleResolver
import com.nickdferrara.fitify.subscription.internal.repository.PaymentHistoryRepository
import com.nickdferrara.fitify.subscription.internal.repository.SubscriptionRepository
import com.nickdferrara.fitify.subscription.internal.service.interfaces.StripeWebhookHandler
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
internal class StripeWebhookHandlerImpl(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentHistoryRepository: PaymentHistoryRepository,
    private val lifecycleResolver: SubscriptionLifecycleResolver,
    private val stripeGateway: StripePaymentGateway,
    private val eventPublisher: ApplicationEventPublisher,
) : StripeWebhookHandler {

    @Transactional
    override fun handleSubscriptionCreated(stripeSubscriptionId: String, customerId: String, metadata: Map<String, String>) {
        val userId = UUID.fromString(metadata["userId"])
        val planType = PlanType.valueOf(metadata["planType"] ?: "MONTHLY")

        val details = stripeGateway.retrieveSubscription(stripeSubscriptionId)

        val subscription = Subscription(
            userId = userId,
            planType = planType,
            stripeSubscriptionId = stripeSubscriptionId,
        )

        val lifecycle = lifecycleResolver.resolve(planType)
        lifecycle.activate(
            subscription,
            Instant.ofEpochSecond(details.currentPeriodStart),
            Instant.ofEpochSecond(details.currentPeriodEnd),
        )

        val saved = subscriptionRepository.save(subscription)

        eventPublisher.publishEvent(
            SubscriptionCreatedEvent(
                subscriptionId = saved.id!!,
                userId = saved.userId,
                planType = saved.planType.name,
                stripeSubscriptionId = stripeSubscriptionId,
                expiresAt = saved.currentPeriodEnd!!,
            )
        )
    }

    @Transactional
    override fun handleSubscriptionRenewed(stripeSubscriptionId: String, amountPaid: Long, paymentIntentId: String?) {
        val subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
            ?: return

        val details = stripeGateway.retrieveSubscription(stripeSubscriptionId)
        val lifecycle = lifecycleResolver.resolve(subscription.planType)
        lifecycle.renew(
            subscription,
            Instant.ofEpochSecond(details.currentPeriodStart),
            Instant.ofEpochSecond(details.currentPeriodEnd),
        )
        subscriptionRepository.save(subscription)

        paymentHistoryRepository.save(
            PaymentHistory(
                userId = subscription.userId,
                subscription = subscription,
                stripePaymentIntentId = paymentIntentId,
                amount = BigDecimal.valueOf(amountPaid, 2),
                status = "succeeded",
            )
        )

        eventPublisher.publishEvent(
            SubscriptionRenewedEvent(
                subscriptionId = subscription.id!!,
                userId = subscription.userId,
                newPeriodEnd = subscription.currentPeriodEnd!!,
                planType = subscription.planType.name,
            )
        )
    }

    @Transactional
    override fun handleSubscriptionExpired(stripeSubscriptionId: String) {
        val subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
            ?: return

        val lifecycle = lifecycleResolver.resolve(subscription.planType)
        lifecycle.expire(subscription)
        subscriptionRepository.save(subscription)

        eventPublisher.publishEvent(
            SubscriptionExpiredEvent(
                subscriptionId = subscription.id!!,
                userId = subscription.userId,
            )
        )
    }

    @Transactional
    override fun handlePaymentFailed(stripeSubscriptionId: String, paymentIntentId: String?) {
        val subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
            ?: return

        subscription.status = SubscriptionStatus.PAST_DUE
        subscriptionRepository.save(subscription)

        paymentHistoryRepository.save(
            PaymentHistory(
                userId = subscription.userId,
                subscription = subscription,
                stripePaymentIntentId = paymentIntentId,
                amount = BigDecimal.ZERO,
                status = "failed",
            )
        )
    }
}
