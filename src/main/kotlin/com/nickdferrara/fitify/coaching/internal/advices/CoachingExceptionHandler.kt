package com.nickdferrara.fitify.coaching.internal.advices

import com.nickdferrara.fitify.coaching.internal.controller.AdminCoachController
import com.nickdferrara.fitify.coaching.internal.controller.AdminLocationCoachController
import com.nickdferrara.fitify.coaching.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.coaching.internal.dtos.response.ValidationErrorResponse
import com.nickdferrara.fitify.coaching.internal.service.CoachNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminCoachController::class, AdminLocationCoachController::class])
internal class CoachingExceptionHandler {

    @ExceptionHandler(CoachNotFoundException::class)
    fun handleNotFound(ex: CoachNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Coach not found"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse("Validation failed", errors))
    }
}
