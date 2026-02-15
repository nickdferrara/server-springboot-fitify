package com.nickdferrara.fitify.admin.internal.advices

import com.nickdferrara.fitify.admin.internal.controller.AdminClassController
import com.nickdferrara.fitify.admin.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.ValidationErrorResponse
import com.nickdferrara.fitify.admin.internal.exceptions.ClassNotFoundException
import com.nickdferrara.fitify.admin.internal.exceptions.CoachNotFoundException
import com.nickdferrara.fitify.admin.internal.exceptions.CoachScheduleConflictException
import com.nickdferrara.fitify.admin.internal.exceptions.InvalidRecurringScheduleException
import com.nickdferrara.fitify.admin.internal.exceptions.LocationNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminClassController::class])
internal class AdminExceptionHandler {

    @ExceptionHandler(LocationNotFoundException::class)
    fun handleLocationNotFound(ex: LocationNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Location not found"))
    }

    @ExceptionHandler(CoachNotFoundException::class)
    fun handleCoachNotFound(ex: CoachNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Coach not found"))
    }

    @ExceptionHandler(ClassNotFoundException::class)
    fun handleClassNotFound(ex: ClassNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Class not found"))
    }

    @ExceptionHandler(CoachScheduleConflictException::class)
    fun handleCoachConflict(ex: CoachScheduleConflictException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.message ?: "Coach schedule conflict"))
    }

    @ExceptionHandler(InvalidRecurringScheduleException::class)
    fun handleInvalidSchedule(ex: InvalidRecurringScheduleException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Invalid recurring schedule"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse("Validation failed", errors))
    }
}
