package com.nickdferrara.fitify.security.internal.advices

import com.nickdferrara.fitify.security.internal.controller.LocationAdminAssignmentController
import com.nickdferrara.fitify.security.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.security.internal.dtos.response.ValidationErrorResponse
import com.nickdferrara.fitify.security.internal.exception.AssignmentAlreadyExistsException
import com.nickdferrara.fitify.security.internal.exception.AssignmentNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [LocationAdminAssignmentController::class])
internal class SecurityExceptionHandler {

    @ExceptionHandler(AssignmentAlreadyExistsException::class)
    fun handleAlreadyExists(ex: AssignmentAlreadyExistsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.message ?: "Assignment already exists"))
    }

    @ExceptionHandler(AssignmentNotFoundException::class)
    fun handleNotFound(ex: AssignmentNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Assignment not found"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse("Validation failed", errors))
    }
}
