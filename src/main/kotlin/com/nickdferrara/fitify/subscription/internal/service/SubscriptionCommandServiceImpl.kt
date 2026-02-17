package com.nickdferrara.fitify.subscription.internal.service

import com.nickdferrara.fitify.subscription.SubscriptionCancelledEvent
import com.nickdferrara.fitify.subscription.internal.config.StripeProperties
import com.nickdferrara.fitify.subscription.internal.discount.DiscountContext
import com.nickdferrara.fitify.subscription.internal.discount.DiscountStrategyResolver
import com.nickdferrara.fitify.subscription.internal.dtos.request.ChangePlanRequest
import com.nickdferrara.fitify.subscription.internal.dtos.request.CheckoutRequest
import com.nickdferrara.fitify.subscription.internal.dtos.response.BillingPortalResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.CheckoutResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionPlanResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionResponse
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import com.nickdferrara.fitify.subscription.internal.exception.ActiveSubscriptionExistsException
import com.nickdferrara.fitify.subscription.internal.exception.StripeException
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionNotFoundException
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionPlanNotFoundException
import com.nickdferrara.fitify.subscription.internal.extensions.toResponse
import com.nickdferrara.fitify.subscription.internal.gateway.StripePaymentGateway
import com.nickdferrara.fitify.subscription.internal.lifecycle.SubscriptionLifecycleResolver
import com.nickdferrara.fitify.subscription.internal.repository.SubscriptionPlanRepository
import com.nickdferrara.fitify.subscription.internal.repository.SubscriptionRepository
import com.nickdferrara.fitify.subscription.internal.service.interfaces.SubscriptionCommandService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
internal class SubscriptionCommandServiceImpl(
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionPlanRepository: SubscriptionPlanRepository,
    private val discountStrategyResolver: DiscountStrategyResolver,
    private val lifecycleResolver: SubscriptionLifecycleResolver,
    private val stripeGateway: StripePaymentGateway,
    private val stripeProperties: StripeProperties,
    private val eventPublisher: ApplicationEventPublisher,
) : SubscriptionCommandService {

    override fun getAvailablePlans(): List<SubscriptionPlanResponse> {
        return subscriptionPlanRepository.findByActiveTrue().map { it.toResponse() }
    }

    override fun getCurrentSubscription(userId: UUID): SubscriptionResponse {
        val subscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        ) ?: throw SubscriptionNotFoundException("No active subscription found for user: $userId")
        return subscription.toResponse()
    }

    @Transactional
    override fun createCheckoutSession(userId: UUID, request: CheckoutRequest): CheckoutResponse {
        val hasActive = subscriptionRepository.existsByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        )
        if (hasActive) {
            throw ActiveSubscriptionExistsException("User already has an active subscription")
        }

        val plan = subscriptionPlanRepository.findById(request.planId)
            .orElseThrow { SubscriptionPlanNotFoundException("Plan not found: ${request.planId}") }

        val subscriptionMonths = calculateSubscriptionMonths(userId)
        val discountContext = DiscountContext(
            userId = userId,
            planType = plan.planType,
            basePrice = plan.basePrice,
            promotionalCode = request.promotionalCode,
            subscriptionMonths = subscriptionMonths,
        )
        val discountAmount = discountStrategyResolver.resolveDiscount(discountContext)

        val couponId = if (discountAmount > BigDecimal.ZERO) {
            val cents = discountAmount.movePointRight(2).toLong()
            stripeGateway.createCoupon(cents, "usd")
        } else {
            null
        }

        val metadata = mapOf(
            "userId" to userId.toString(),
            "planType" to plan.planType.name,
            "discountAmount" to discountAmount.toPlainString(),
        )

        val result = stripeGateway.createCheckoutSession(
            stripePriceId = plan.stripePriceId,
            successUrl = stripeProperties.successUrl,
            cancelUrl = stripeProperties.cancelUrl,
            metadata = metadata,
            couponId = couponId,
        )

        return CheckoutResponse(
            sessionId = result.sessionId,
            url = result.url,
        )
    }

    @Transactional
    override fun cancelSubscription(userId: UUID): SubscriptionResponse {
        val subscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE)
        ) ?: throw SubscriptionNotFoundException("No active subscription found for user: $userId")

        val stripeSubId = subscription.stripeSubscriptionId
        if (stripeSubId != null) {
            stripeGateway.cancelSubscriptionAtPeriodEnd(stripeSubId)
        }

        val lifecycle = lifecycleResolver.resolve(subscription.planType)
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
    override fun changePlan(userId: UUID, request: ChangePlanRequest): CheckoutResponse {
        val currentSubscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        )

        if (currentSubscription?.stripeSubscriptionId != null) {
            stripeGateway.cancelSubscriptionImmediately(currentSubscription.stripeSubscriptionId!!)
            currentSubscription.status = SubscriptionStatus.EXPIRED
            subscriptionRepository.save(currentSubscription)
        }

        return createCheckoutSession(userId, CheckoutRequest(planId = request.newPlanId))
    }

    override fun createBillingPortalSession(userId: UUID): BillingPortalResponse {
        val subscription = subscriptionRepository.findByUserIdAndStatusIn(
            userId, listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING)
        ) ?: throw SubscriptionNotFoundException("No active subscription found for user: $userId")

        val stripeSubId = subscription.stripeSubscriptionId
            ?: throw StripeException("No Stripe subscription linked")

        val details = stripeGateway.retrieveSubscription(stripeSubId)
        val url = stripeGateway.createBillingPortalSession(details.customer, stripeProperties.successUrl)
        return BillingPortalResponse(url = url)
    }

    private fun calculateSubscriptionMonths(userId: UUID): Long {
        val subscriptions = subscriptionRepository.findByUserId(userId)
        val earliest = subscriptions.mapNotNull { it.createdAt }.minOrNull() ?: return 0
        return ChronoUnit.MONTHS.between(earliest, Instant.now())
    }
}
