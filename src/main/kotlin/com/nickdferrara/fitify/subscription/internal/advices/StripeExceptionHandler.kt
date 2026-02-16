package com.nickdferrara.fitify.subscription.internal.advices

import com.nickdferrara.fitify.subscription.internal.controller.StripeWebhookController
import com.nickdferrara.fitify.subscription.internal.controller.SubscriptionController
import com.nickdferrara.fitify.subscription.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.subscription.internal.exception.StripeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [SubscriptionController::class, StripeWebhookController::class])
internal class StripeExceptionHandler {

    @ExceptionHandler(StripeException::class)
    fun handleStripeException(ex: StripeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(ErrorResponse(ex.message ?: "Stripe service error"))
    }
}
