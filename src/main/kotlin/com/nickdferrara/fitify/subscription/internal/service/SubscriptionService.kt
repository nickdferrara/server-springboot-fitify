package com.nickdferrara.fitify.subscription.internal.service

import com.nickdferrara.fitify.subscription.SubscriptionApi
import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import com.nickdferrara.fitify.subscription.SubscriptionExpiredEvent
import com.nickdferrara.fitify.subscription.SubscriptionRenewedEvent
import com.nickdferrara.fitify.subscription.SubscriptionSummary
import com.nickdferrara.fitify.subscription.internal.config.StripeProperties
import com.nickdferrara.fitify.subscription.internal.discount.DiscountContext
import com.nickdferrara.fitify.subscription.internal.discount.DiscountStrategyResolver
import com.nickdferrara.fitify.subscription.internal.dtos.request.ChangePlanRequest
import com.nickdferrara.fitify.subscription.internal.dtos.request.CheckoutRequest
import com.nickdferrara.fitify.subscription.internal.dtos.response.BillingPortalResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.CheckoutResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionPlanResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.toResponse
import com.nickdferrara.fitify.subscription.internal.entities.PaymentHistory
import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import com.nickdferrara.fitify.subscription.internal.entities.Subscription
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import com.nickdferrara.fitify.subscription.internal.exception.ActiveSubscriptionExistsException
import com.nickdferrara.fitify.subscription.internal.exception.StripeException
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionNotFoundException
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionPlanNotFoundException
import com.nickdferrara.fitify.subscription.internal.lifecycle.AnnualSubscriptionLifecycle
import com.nickdferrara.fitify.subscription.internal.lifecycle.MonthlySubscriptionLifecycle
import com.nickdferrara.fitify.subscription.internal.lifecycle.SubscriptionLifecycleTemplate
import com.nickdferrara.fitify.subscription.internal.repository.PaymentHistoryRepository
import com.nickdferrara.fitify.subscription.internal.repository.SubscriptionPlanRepository
import com.nickdferrara.fitify.subscription.internal.repository.SubscriptionRepository
import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.billingportal.SessionCreateParams as PortalSessionCreateParams
import com.stripe.param.checkout.SessionCreateParams
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
@EnableConfigurationProperties(StripeProperties::class)
internal class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val paymentHistoryRepository: PaymentHistoryRepository,
    private val discountStrategyResolver: DiscountStrategyResolver,
    private val monthlyLifecycle: MonthlySubscriptionLifecycle,
    private val annualLifecycle: AnnualSubscriptionLifecycle,
    private val stripeProperties: StripeProperties,
    private val eventPublisher: ApplicationEventPublisher,
) : SubscriptionApi {

    @PostConstruct
    fun init() {
        Stripe.apiKey = stripeProperties.secretKey
    }

    // --- SubscriptionApi (cross-module) ---

    override fun findActiveSubscriptionByUserId(userId: UUID): SubscriptionSummary? {
        val subscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        ) ?: return null
        return subscription.toSummary()
    }

    override fun hasActiveSubscription(userId: UUID): Boolean {
        return subscriptionRepository.existsByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        )
    }

    override fun countActiveSubscriptionsByPlanType(): Map<String, Long> {
        return subscriptionRepository
            .countByStatusInGroupByPlanType(listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING))
            .associate { (it[0] as PlanType).name to it[1] as Long }
    }

    override fun countSubscriptionsExpiredBetween(start: Instant, end: Instant): Long {
        return subscriptionRepository.countExpiredBetween(start, end)
    }

    override fun countActiveSubscriptionsAsOf(asOf: Instant): Long {
        return subscriptionRepository.countActiveAsOf(asOf)
    }

    override fun sumRevenueBetween(start: Instant, end: Instant): BigDecimal {
        return paymentHistoryRepository.sumRevenueBetween(start, end)
    }

    override fun sumRevenueBetweenByPlanType(start: Instant, end: Instant): Map<String, BigDecimal> {
        return paymentHistoryRepository
            .sumRevenueBetweenGroupByPlanType(start, end)
            .associate { (it[0] as PlanType).name to it[1] as BigDecimal }
    }

    // --- User-facing methods ---

    fun getAvailablePlans(): List<SubscriptionPlanResponse> {
        return subscriptionPlanRepository.findByActiveTrue().map { it.toResponse() }
    }

    fun getCurrentSubscription(userId: UUID): SubscriptionResponse {
        val subscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        ) ?: throw SubscriptionNotFoundException("No active subscription found for user: $userId")
        return subscription.toResponse()
    }

    @Transactional
    fun createCheckoutSession(userId: UUID, request: CheckoutRequest): CheckoutResponse {
        if (hasActiveSubscription(userId)) {
            throw ActiveSubscriptionExistsException("User already has an active subscription")
        }

        val plan = subscriptionPlanRepository.findById(request.planId)
            .orElseThrow { SubscriptionPlanNotFoundException("Plan not found: ${request.planId}") }

        try {
            val params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(stripeProperties.successUrl)
                .setCancelUrl(stripeProperties.cancelUrl)
                .putMetadata("userId", userId.toString())
                .putMetadata("planType", plan.planType.name)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(plan.stripePriceId)
                        .setQuantity(1)
                        .build()
                )
                .build()

            val session = Session.create(params)
            return CheckoutResponse(
                sessionId = session.id,
                url = session.url,
            )
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to create checkout session", e)
        }
    }

    @Transactional
    fun cancelSubscription(userId: UUID): SubscriptionResponse {
        val subscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE)
        ) ?: throw SubscriptionNotFoundException("No active subscription found for user: $userId")

        val stripeSubId = subscription.stripeSubscriptionId
        if (stripeSubId != null) {
            try {
                val stripeSub = com.stripe.model.Subscription.retrieve(stripeSubId)
                val params = com.stripe.param.SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build()
                stripeSub.update(params)
            } catch (e: com.stripe.exception.StripeException) {
                throw StripeException("Failed to cancel subscription on Stripe", e)
            }
        }

        val lifecycle = lifecycleFor(subscription.planType)
        lifecycle.cancel(subscription)
        val saved = subscriptionRepository.save(subscription)

        eventPublisher.publishEvent(
            SubscriptionCancelledEvent(
                subscriptionId = saved.id!!,
                userId = saved.userId,
                effectiveDate = saved.currentPeriodEnd ?: Instant.now(),
            )
        )

        return saved.toResponse()
    }

    @Transactional
    fun changePlan(userId: UUID, request: ChangePlanRequest): CheckoutResponse {
        val currentSubscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        )

        if (currentSubscription?.stripeSubscriptionId != null) {
            try {
                val stripeSub = com.stripe.model.Subscription.retrieve(currentSubscription.stripeSubscriptionId)
                stripeSub.cancel()
            } catch (e: com.stripe.exception.StripeException) {
                throw StripeException("Failed to cancel existing subscription on Stripe", e)
            }
            currentSubscription.status = SubscriptionStatus.EXPIRED
            subscriptionRepository.save(currentSubscription)
        }

        return createCheckoutSession(userId, CheckoutRequest(planId = request.newPlanId))
    }

    fun createBillingPortalSession(userId: UUID): BillingPortalResponse {
        val subscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        ) ?: throw SubscriptionNotFoundException("No active subscription found for user: $userId")

        val stripeSubId = subscription.stripeSubscriptionId
            ?: throw StripeException("No Stripe subscription linked")

        try {
            val stripeSub = com.stripe.model.Subscription.retrieve(stripeSubId)
            val params = PortalSessionCreateParams.builder()
                .setCustomer(stripeSub.customer)
                .setReturnUrl(stripeProperties.successUrl)
                .build()

            val portalSession = com.stripe.model.billingportal.Session.create(params)
            return BillingPortalResponse(url = portalSession.url)
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to create billing portal session", e)
        }
    }

    // --- Webhook handlers ---

    @Transactional
    fun handleSubscriptionCreated(stripeSubscriptionId: String, customerId: String, metadata: Map<String, String>) {
        val userId = UUID.fromString(metadata["userId"])
        val planType = PlanType.valueOf(metadata["planType"] ?: "MONTHLY")

        try {
            val stripeSub = com.stripe.model.Subscription.retrieve(stripeSubscriptionId)

            val subscription = Subscription(
                userId = userId,
                planType = planType,
                stripeSubscriptionId = stripeSubscriptionId,
            )

            val lifecycle = lifecycleFor(planType)
            lifecycle.activate(
                subscription,
                Instant.ofEpochSecond(stripeSub.currentPeriodStart),
                Instant.ofEpochSecond(stripeSub.currentPeriodEnd),
            )

            val saved = subscriptionRepository.save(subscription)

            eventPublisher.publishEvent(
                SubscriptionCreatedEvent(
                    subscriptionId = saved.id!!,
                    userId = saved.userId,
                    planType = saved.planType.name,
                    stripeSubscriptionId = stripeSubscriptionId,
                )
            )
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to retrieve subscription from Stripe", e)
        }
    }

    @Transactional
    fun handleSubscriptionRenewed(stripeSubscriptionId: String, amountPaid: Long, paymentIntentId: String?) {
        val subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
            ?: return

        try {
            val stripeSub = com.stripe.model.Subscription.retrieve(stripeSubscriptionId)
            val lifecycle = lifecycleFor(subscription.planType)
            lifecycle.renew(
                subscription,
                Instant.ofEpochSecond(stripeSub.currentPeriodStart),
                Instant.ofEpochSecond(stripeSub.currentPeriodEnd),
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
                )
            )
        } catch (e: com.stripe.exception.StripeException) {
            throw StripeException("Failed to retrieve subscription from Stripe", e)
        }
    }

    @Transactional
    fun handleSubscriptionExpired(stripeSubscriptionId: String) {
        val subscription = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
            ?: return

        val lifecycle = lifecycleFor(subscription.planType)
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
    fun handlePaymentFailed(stripeSubscriptionId: String, paymentIntentId: String?) {
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

    // --- Helpers ---

    private fun lifecycleFor(planType: PlanType): SubscriptionLifecycleTemplate {
        return when (planType) {
            PlanType.MONTHLY -> monthlyLifecycle
            PlanType.ANNUAL -> annualLifecycle
        }
    }

    private fun Subscription.toSummary() = SubscriptionSummary(
        id = id!!,
        userId = userId,
        planType = planType.name,
        status = status.name,
        currentPeriodEnd = currentPeriodEnd,
    )
}
