package com.nickdferrara.fitify.coaching.internal.controller

import com.nickdferrara.fitify.TestSecurityConfig
import com.nickdferrara.fitify.coaching.internal.service.CoachingService
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(AdminCoachController::class)
@Import(TestSecurityConfig::class)
internal class AdminCoachControllerValidationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var coachingService: CoachingService

    @Test
    fun `create coach with blank fields returns 400`() {
        mockMvc.perform(
            post("/api/v1/admin/coaches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","bio":""}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.bio").exists())
    }

    @Test
    fun `create coach with invalid certification returns 400`() {
        mockMvc.perform(
            post("/api/v1/admin/coaches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name":"Jane","bio":"Coach bio",
                        "certifications":[{"name":"","issuer":""}]
                    }
                """.trimIndent())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors['certifications[0].name']").exists())
            .andExpect(jsonPath("$.errors['certifications[0].issuer']").exists())
    }

    @Test
    fun `assign locations with empty list returns 400`() {
        val coachId = UUID.randomUUID()
        mockMvc.perform(
            put("/api/v1/admin/coaches/$coachId/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"locationIds":[]}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.locationIds").exists())
    }
}
