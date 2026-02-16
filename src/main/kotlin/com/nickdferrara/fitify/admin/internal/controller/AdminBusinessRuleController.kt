package com.nickdferrara.fitify.admin.internal.controller

import com.nickdferrara.fitify.admin.internal.service.interfaces.AdminService
import com.nickdferrara.fitify.admin.internal.dtos.request.UpdateBusinessRuleRequest
import com.nickdferrara.fitify.admin.internal.dtos.response.BusinessRuleResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/business-rules")
@PreAuthorize("hasRole('ADMIN')")
internal class AdminBusinessRuleController(
    private val adminService: AdminService,
) {

    @GetMapping
    fun listBusinessRules(): ResponseEntity<List<BusinessRuleResponse>> {
        return ResponseEntity.ok(adminService.listBusinessRules())
    }

    @PutMapping("/{ruleKey}")
    fun updateBusinessRule(
        @PathVariable ruleKey: String,
        @Valid @RequestBody request: UpdateBusinessRuleRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<BusinessRuleResponse> {
        val updatedBy = jwt.subject
        return ResponseEntity.ok(adminService.updateBusinessRule(ruleKey, request, updatedBy))
    }
}
