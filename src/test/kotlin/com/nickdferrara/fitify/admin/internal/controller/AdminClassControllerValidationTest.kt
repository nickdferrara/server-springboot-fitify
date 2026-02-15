package com.nickdferrara.fitify.admin.internal.controller

import com.nickdferrara.fitify.TestSecurityConfig
import com.nickdferrara.fitify.admin.internal.AdminService
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

@WebMvcTest(AdminClassController::class)
@Import(TestSecurityConfig::class)
internal class AdminClassControllerValidationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var adminService: AdminService

    @Test
    fun `create class with blank name and negative capacity returns 400`() {
        val locationId = UUID.randomUUID()
        mockMvc.perform(
            post("/api/v1/admin/locations/$locationId/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name":"","classType":"","coachId":"${UUID.randomUUID()}",
                        "startTime":"2025-06-01T09:00:00Z","endTime":"2025-06-01T10:00:00Z",
                        "capacity":-1
                    }
                """.trimIndent())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.classType").exists())
            .andExpect(jsonPath("$.errors.capacity").exists())
    }

    @Test
    fun `update class with negative capacity returns 400`() {
        val classId = UUID.randomUUID()
        mockMvc.perform(
            put("/api/v1/admin/classes/$classId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"capacity":-5}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.capacity").exists())
    }

    @Test
    fun `create recurring schedule with blank fields and negative values returns 400`() {
        val locationId = UUID.randomUUID()
        mockMvc.perform(
            post("/api/v1/admin/locations/$locationId/classes/recurring")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name":"","classType":"","coachId":"${UUID.randomUUID()}",
                        "daysOfWeek":[],"startTime":"09:00",
                        "durationMinutes":-30,"capacity":0,
                        "startDate":"2025-06-01","endDate":"2025-06-30"
                    }
                """.trimIndent())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.classType").exists())
            .andExpect(jsonPath("$.errors.daysOfWeek").exists())
    }
}
