package com.nickdferrara.fitify.admin.internal.advices

import com.nickdferrara.fitify.admin.internal.controller.AdminClassController
import com.nickdferrara.fitify.admin.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.admin.internal.exceptions.ClassNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminClassController::class])
internal class ClassNotFoundExceptionHandler {

    @ExceptionHandler(ClassNotFoundException::class)
    fun handleClassNotFound(ex: ClassNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Class not found"))
    }
}
