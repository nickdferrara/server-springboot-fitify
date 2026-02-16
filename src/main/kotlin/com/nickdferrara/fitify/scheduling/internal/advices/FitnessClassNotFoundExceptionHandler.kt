package com.nickdferrara.fitify.scheduling.internal.advices

import com.nickdferrara.fitify.scheduling.internal.controller.ClassController
import com.nickdferrara.fitify.scheduling.internal.controller.WaitlistController
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.scheduling.internal.exceptions.FitnessClassNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [ClassController::class, WaitlistController::class])
internal class FitnessClassNotFoundExceptionHandler {

    @ExceptionHandler(FitnessClassNotFoundException::class)
    fun handleClassNotFound(ex: FitnessClassNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Fitness class not found"))
    }
}
