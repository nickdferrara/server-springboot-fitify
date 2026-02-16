package com.nickdferrara.fitify.security.internal.advices

import com.nickdferrara.fitify.security.internal.controller.LocationAdminAssignmentController
import com.nickdferrara.fitify.security.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.security.internal.exception.AssignmentNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [LocationAdminAssignmentController::class])
internal class AssignmentNotFoundExceptionHandler {

    @ExceptionHandler(AssignmentNotFoundException::class)
    fun handleNotFound(ex: AssignmentNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Assignment not found"))
    }
}
