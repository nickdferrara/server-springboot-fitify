package com.nickdferrara.fitify.subscription.internal.advices

import com.nickdferrara.fitify.subscription.internal.controller.StripeWebhookController
import com.nickdferrara.fitify.subscription.internal.controller.SubscriptionController
import com.nickdferrara.fitify.subscription.internal.dtos.response.ValidationErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [SubscriptionController::class, StripeWebhookController::class])
internal class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse("Validation failed", errors))
    }
}
