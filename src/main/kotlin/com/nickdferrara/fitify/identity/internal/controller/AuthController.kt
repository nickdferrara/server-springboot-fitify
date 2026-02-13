package com.nickdferrara.fitify.identity.internal.controller

import com.nickdferrara.fitify.identity.internal.dtos.request.ForgotPasswordRequest
import com.nickdferrara.fitify.identity.internal.dtos.request.RegisterRequest
import com.nickdferrara.fitify.identity.internal.dtos.request.ResetPasswordRequest
import com.nickdferrara.fitify.identity.internal.dtos.response.MessageResponse
import com.nickdferrara.fitify.identity.internal.dtos.response.RegisterResponse
import com.nickdferrara.fitify.identity.internal.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
internal class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(authService.forgotPassword(request))
    }

    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(authService.resetPassword(request))
    }
}
