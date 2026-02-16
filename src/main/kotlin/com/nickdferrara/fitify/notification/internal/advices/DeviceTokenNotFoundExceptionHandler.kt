package com.nickdferrara.fitify.notification.internal.advices

import com.nickdferrara.fitify.notification.internal.controller.DeviceTokenController
import com.nickdferrara.fitify.notification.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.notification.internal.exception.DeviceTokenNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [DeviceTokenController::class])
internal class DeviceTokenNotFoundExceptionHandler {

    @ExceptionHandler(DeviceTokenNotFoundException::class)
    fun handleDeviceTokenNotFound(ex: DeviceTokenNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Device token not found"))
    }
}
