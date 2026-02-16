package com.nickdferrara.fitify.identity.internal.advices

import com.nickdferrara.fitify.identity.internal.controller.AuthController
import com.nickdferrara.fitify.identity.internal.controller.UserController
import com.nickdferrara.fitify.identity.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.identity.internal.exception.IdentityProviderException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AuthController::class, UserController::class])
internal class IdentityProviderExceptionHandler {

    @ExceptionHandler(IdentityProviderException::class)
    fun handleIdentityProvider(ex: IdentityProviderException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse(ex.message ?: "Identity provider unavailable"))
    }
}
