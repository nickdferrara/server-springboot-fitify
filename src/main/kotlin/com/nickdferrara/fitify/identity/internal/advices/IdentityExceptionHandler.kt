package com.nickdferrara.fitify.identity.internal.advices

import com.nickdferrara.fitify.identity.internal.controller.AuthController
import com.nickdferrara.fitify.identity.internal.controller.UserController
import com.nickdferrara.fitify.identity.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.identity.internal.exception.EmailAlreadyExistsException
import com.nickdferrara.fitify.identity.internal.exception.InvalidTokenException
import com.nickdferrara.fitify.identity.internal.exception.UserNotFoundException
import com.nickdferrara.fitify.identity.internal.exception.IdentityProviderException
import com.nickdferrara.fitify.identity.internal.exception.WeakPasswordException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AuthController::class, UserController::class])
internal class IdentityExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "User not found"))
    }

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleConflict(ex: EmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.message ?: "Email already exists"))
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidToken(ex: InvalidTokenException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Invalid token"))
    }

    @ExceptionHandler(WeakPasswordException::class)
    fun handleWeakPassword(ex: WeakPasswordException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Weak password"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Invalid argument"))
    }

    @ExceptionHandler(IdentityProviderException::class)
    fun handleIdentityProvider(ex: IdentityProviderException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse(ex.message ?: "Identity provider unavailable"))
    }
}
