package com.nickdferrara.fitify.admin.internal.advices

import com.nickdferrara.fitify.admin.internal.controller.AdminBusinessRuleController
import com.nickdferrara.fitify.admin.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.admin.internal.exceptions.BusinessRuleNotFoundException
import com.nickdferrara.fitify.admin.internal.exceptions.InvalidBusinessRuleValueException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [AdminBusinessRuleController::class])
internal class BusinessRuleExceptionHandler {

    @ExceptionHandler(BusinessRuleNotFoundException::class)
    fun handleBusinessRuleNotFound(ex: BusinessRuleNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Business rule not found"))
    }

    @ExceptionHandler(InvalidBusinessRuleValueException::class)
    fun handleInvalidBusinessRuleValue(ex: InvalidBusinessRuleValueException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Invalid business rule value"))
    }
}
