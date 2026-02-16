package com.nickdferrara.fitify.identity.internal.controller

import com.nickdferrara.fitify.identity.internal.service.interfaces.IdentityService
import com.nickdferrara.fitify.identity.internal.dtos.request.UpdatePreferencesRequest
import com.nickdferrara.fitify.identity.internal.dtos.response.UserPreferencesResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/me")
internal class UserController(
    private val identityService: IdentityService,
) {

    @GetMapping("/preferences")
    fun getPreferences(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<UserPreferencesResponse> {
        return ResponseEntity.ok(identityService.getPreferences(jwt.subject))
    }

    @PatchMapping("/preferences")
    fun updatePreferences(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: UpdatePreferencesRequest,
    ): ResponseEntity<UserPreferencesResponse> {
        return ResponseEntity.ok(identityService.updatePreferences(jwt.subject, request))
    }
}
