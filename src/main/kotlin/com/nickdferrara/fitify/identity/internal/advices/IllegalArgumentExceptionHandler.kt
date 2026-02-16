package com.nickdferrara.fitify.identity.internal.advices

import com.nickdferrara.fitify.identity.internal.controller.AuthController
import com.nickdferrara.fitify.identity.internal.controller.UserController
import com.nickdferrara.fitify.identity.internal.dtos.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AuthController::class, UserController::class])
internal class IllegalArgumentExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Invalid argument"))
    }
}
