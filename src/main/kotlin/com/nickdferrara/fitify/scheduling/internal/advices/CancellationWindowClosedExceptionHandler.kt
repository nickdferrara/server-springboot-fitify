package com.nickdferrara.fitify.scheduling.internal.advices

import com.nickdferrara.fitify.scheduling.internal.controller.ClassController
import com.nickdferrara.fitify.scheduling.internal.controller.WaitlistController
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.scheduling.internal.exceptions.CancellationWindowClosedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [ClassController::class, WaitlistController::class])
internal class CancellationWindowClosedExceptionHandler {

    @ExceptionHandler(CancellationWindowClosedException::class)
    fun handleCancellationWindowClosed(ex: CancellationWindowClosedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Cancellation window closed"))
    }
}
