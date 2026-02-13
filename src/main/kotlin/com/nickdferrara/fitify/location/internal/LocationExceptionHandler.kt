package com.nickdferrara.fitify.location.internal

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminLocationController::class, LocationController::class])
internal class LocationExceptionHandler {

    @ExceptionHandler(LocationNotFoundException::class)
    fun handleNotFound(ex: LocationNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Location not found"))
    }
}

internal data class ErrorResponse(val message: String)
