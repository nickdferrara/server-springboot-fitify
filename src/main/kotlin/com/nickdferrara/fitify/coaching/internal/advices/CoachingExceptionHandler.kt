package com.nickdferrara.fitify.coaching.internal.advices

import com.nickdferrara.fitify.coaching.internal.controller.AdminCoachController
import com.nickdferrara.fitify.coaching.internal.controller.AdminLocationCoachController
import com.nickdferrara.fitify.coaching.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.coaching.internal.service.CoachNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminCoachController::class, AdminLocationCoachController::class])
internal class CoachingExceptionHandler {

    @ExceptionHandler(CoachNotFoundException::class)
    fun handleNotFound(ex: CoachNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Coach not found"))
    }
}
