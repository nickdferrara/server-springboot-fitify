package com.nickdferrara.fitify.subscription.internal.advices

import com.nickdferrara.fitify.subscription.internal.controller.StripeWebhookController
import com.nickdferrara.fitify.subscription.internal.controller.SubscriptionController
import com.nickdferrara.fitify.subscription.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.subscription.internal.exception.ActiveSubscriptionExistsException
import com.nickdferrara.fitify.subscription.internal.exception.InvalidWebhookSignatureException
import com.nickdferrara.fitify.subscription.internal.exception.StripeException
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionNotFoundException
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionPlanNotFoundException
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionStateException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(
    assignableTypes = [
        SubscriptionController::class,
        StripeWebhookController::class,
    ]
)
internal class SubscriptionExceptionHandler {

    @ExceptionHandler(SubscriptionNotFoundException::class)
    fun handleNotFound(ex: SubscriptionNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Subscription not found"))
    }

    @ExceptionHandler(SubscriptionPlanNotFoundException::class)
    fun handlePlanNotFound(ex: SubscriptionPlanNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Subscription plan not found"))
    }

    @ExceptionHandler(ActiveSubscriptionExistsException::class)
    fun handleConflict(ex: ActiveSubscriptionExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.message ?: "Active subscription already exists"))
    }

    @ExceptionHandler(InvalidWebhookSignatureException::class)
    fun handleInvalidSignature(ex: InvalidWebhookSignatureException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Invalid webhook signature"))
    }

    @ExceptionHandler(SubscriptionStateException::class)
    fun handleStateException(ex: SubscriptionStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse(ex.message ?: "Invalid subscription state transition"))
    }

    @ExceptionHandler(StripeException::class)
    fun handleStripeException(ex: StripeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(ErrorResponse(ex.message ?: "Stripe service error"))
    }
}
