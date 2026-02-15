package com.nickdferrara.fitify.security.internal.controller

import com.nickdferrara.fitify.TestSecurityConfig
import com.nickdferrara.fitify.security.internal.service.LocationAdminService
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(LocationAdminAssignmentController::class)
@Import(TestSecurityConfig::class)
internal class LocationAdminAssignmentControllerValidationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var locationAdminService: LocationAdminService

    @Test
    fun `assign location admin with blank keycloakId returns 400`() {
        mockMvc.perform(
            post("/api/v1/admin/location-admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"keycloakId":"","locationId":"${UUID.randomUUID()}"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.keycloakId").exists())
    }
}
