package com.nickdferrara.fitify.subscription.internal.advices

import com.nickdferrara.fitify.subscription.internal.controller.StripeWebhookController
import com.nickdferrara.fitify.subscription.internal.controller.SubscriptionController
import com.nickdferrara.fitify.subscription.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.subscription.internal.exception.SubscriptionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [SubscriptionController::class, StripeWebhookController::class])
internal class SubscriptionNotFoundExceptionHandler {

    @ExceptionHandler(SubscriptionNotFoundException::class)
    fun handleNotFound(ex: SubscriptionNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Subscription not found"))
    }
}
