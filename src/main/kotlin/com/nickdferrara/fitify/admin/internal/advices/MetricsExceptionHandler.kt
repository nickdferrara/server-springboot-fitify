package com.nickdferrara.fitify.admin.internal.advices

import com.nickdferrara.fitify.admin.internal.controller.AdminLocationMetricsController
import com.nickdferrara.fitify.admin.internal.controller.AdminMetricsController
import com.nickdferrara.fitify.admin.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.admin.internal.exceptions.InvalidMetricsQueryException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminMetricsController::class, AdminLocationMetricsController::class])
internal class MetricsExceptionHandler {

    @ExceptionHandler(InvalidMetricsQueryException::class)
    fun handleInvalidMetricsQuery(ex: InvalidMetricsQueryException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Invalid metrics query"))
    }
}
