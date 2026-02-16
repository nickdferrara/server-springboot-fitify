package com.nickdferrara.fitify.scheduling.internal.advices

import com.nickdferrara.fitify.scheduling.internal.controller.ClassController
import com.nickdferrara.fitify.scheduling.internal.controller.WaitlistController
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [ClassController::class, WaitlistController::class])
internal class OptimisticLockingExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLocking(ex: ObjectOptimisticLockingFailureException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse("Concurrent modification detected. Please retry."))
    }
}
