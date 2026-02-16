package com.nickdferrara.fitify.admin.internal.advices

import com.nickdferrara.fitify.admin.internal.controller.AdminClassController
import com.nickdferrara.fitify.admin.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.admin.internal.exceptions.CoachScheduleConflictException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminClassController::class])
internal class CoachScheduleConflictExceptionHandler {

    @ExceptionHandler(CoachScheduleConflictException::class)
    fun handleCoachConflict(ex: CoachScheduleConflictException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.message ?: "Coach schedule conflict"))
    }
}
