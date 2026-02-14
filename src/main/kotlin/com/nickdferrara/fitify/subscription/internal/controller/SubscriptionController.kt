package com.nickdferrara.fitify.subscription.internal.controller

import com.nickdferrara.fitify.subscription.internal.dtos.request.ChangePlanRequest
import com.nickdferrara.fitify.subscription.internal.dtos.request.CheckoutRequest
import com.nickdferrara.fitify.subscription.internal.dtos.response.BillingPortalResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.CheckoutResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionPlanResponse
import com.nickdferrara.fitify.subscription.internal.dtos.response.SubscriptionResponse
import com.nickdferrara.fitify.subscription.internal.service.SubscriptionService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/subscriptions")
internal class SubscriptionController(
    private val subscriptionService: SubscriptionService,
) {

    @GetMapping("/plans")
    fun getAvailablePlans(): ResponseEntity<List<SubscriptionPlanResponse>> {
        return ResponseEntity.ok(subscriptionService.getAvailablePlans())
    }

    @PostMapping("/checkout")
    fun createCheckoutSession(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CheckoutRequest,
    ): ResponseEntity<CheckoutResponse> {
        val userId = UUID.fromString(jwt.subject)
        return ResponseEntity.ok(subscriptionService.createCheckoutSession(userId, request))
    }

    @GetMapping("/me")
    fun getCurrentSubscription(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SubscriptionResponse> {
        val userId = UUID.fromString(jwt.subject)
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(userId))
    }

    @PostMapping("/me/cancel")
    fun cancelSubscription(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SubscriptionResponse> {
        val userId = UUID.fromString(jwt.subject)
        return ResponseEntity.ok(subscriptionService.cancelSubscription(userId))
    }

    @PostMapping("/me/change-plan")
    fun changePlan(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: ChangePlanRequest,
    ): ResponseEntity<CheckoutResponse> {
        val userId = UUID.fromString(jwt.subject)
        return ResponseEntity.ok(subscriptionService.changePlan(userId, request))
    }

    @PostMapping("/me/billing-portal")
    fun createBillingPortalSession(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<BillingPortalResponse> {
        val userId = UUID.fromString(jwt.subject)
        return ResponseEntity.ok(subscriptionService.createBillingPortalSession(userId))
    }
}
