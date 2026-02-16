package com.nickdferrara.fitify.location.internal.advices

import com.nickdferrara.fitify.location.internal.controller.AdminLocationController
import com.nickdferrara.fitify.location.internal.controller.LocationController
import com.nickdferrara.fitify.location.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.location.internal.service.LocationNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminLocationController::class, LocationController::class])
internal class LocationNotFoundExceptionHandler {

    @ExceptionHandler(LocationNotFoundException::class)
    fun handleNotFound(ex: LocationNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Location not found"))
    }
}
