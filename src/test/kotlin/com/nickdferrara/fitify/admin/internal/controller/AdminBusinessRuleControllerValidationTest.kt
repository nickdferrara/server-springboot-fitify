package com.nickdferrara.fitify.admin.internal.controller

import com.nickdferrara.fitify.TestSecurityConfig
import com.nickdferrara.fitify.admin.internal.service.interfaces.AdminService
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(AdminBusinessRuleController::class)
@Import(TestSecurityConfig::class)
internal class AdminBusinessRuleControllerValidationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var adminService: AdminService

    @Test
    fun `update business rule with blank value returns 400`() {
        mockMvc.perform(
            put("/api/v1/admin/business-rules/MAX_BOOKINGS")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"value":""}""")
                .with(jwt().jwt { it.subject(UUID.randomUUID().toString()) })
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.value").exists())
    }
}
