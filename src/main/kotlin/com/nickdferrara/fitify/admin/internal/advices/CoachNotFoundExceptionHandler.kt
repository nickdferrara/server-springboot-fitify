package com.nickdferrara.fitify.admin.internal.advices

import com.nickdferrara.fitify.admin.internal.controller.AdminClassController
import com.nickdferrara.fitify.admin.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.admin.internal.exceptions.CoachNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminClassController::class])
internal class CoachNotFoundExceptionHandler {

    @ExceptionHandler(CoachNotFoundException::class)
    fun handleCoachNotFound(ex: CoachNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Coach not found"))
    }
}
