package com.nickdferrara.fitify.notification.internal.controller

import com.nickdferrara.fitify.notification.internal.dtos.request.RegisterDeviceTokenRequest
import com.nickdferrara.fitify.notification.internal.dtos.response.DeviceTokenResponse
import com.nickdferrara.fitify.notification.internal.dtos.response.NotificationLogResponse
import com.nickdferrara.fitify.notification.internal.dtos.response.toResponse
import com.nickdferrara.fitify.notification.internal.service.NotificationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
internal class DeviceTokenController(
    private val notificationService: NotificationService,
) {

    @PostMapping("/devices")
    fun registerDeviceToken(
        @Valid @RequestBody request: RegisterDeviceTokenRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Void> {
        val userId = UUID.fromString(jwt.subject)
        notificationService.registerDeviceToken(userId, request.token, request.deviceType)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/devices/{token}")
    fun removeDeviceToken(
        @PathVariable token: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<Void> {
        val userId = UUID.fromString(jwt.subject)
        notificationService.removeDeviceToken(userId, token)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/history")
    fun getNotificationHistory(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<NotificationLogResponse>> {
        val userId = UUID.fromString(jwt.subject)
        val history = notificationService.getNotificationHistory(userId).map { it.toResponse() }
        return ResponseEntity.ok(history)
    }
}
