package com.nickdferrara.fitify.location.internal.controller

import com.nickdferrara.fitify.TestSecurityConfig
import com.nickdferrara.fitify.location.internal.service.LocationService
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

@WebMvcTest(AdminLocationController::class)
@Import(TestSecurityConfig::class)
internal class AdminLocationControllerValidationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var locationService: LocationService

    @Test
    fun `create location with blank fields returns 400`() {
        mockMvc.perform(
            post("/api/v1/admin/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name":"","address":"","city":"","state":"",
                        "zipCode":"","phone":"","email":"","timeZone":"",
                        "operatingHours":[]
                    }
                """.trimIndent())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.address").exists())
            .andExpect(jsonPath("$.errors.city").exists())
            .andExpect(jsonPath("$.errors.state").exists())
            .andExpect(jsonPath("$.errors.email").exists())
    }

    @Test
    fun `create location with invalid state length returns 400`() {
        mockMvc.perform(
            post("/api/v1/admin/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name":"Gym","address":"123 Main","city":"Town","state":"ABC",
                        "zipCode":"12345","phone":"555-1234","email":"gym@example.com","timeZone":"US/Eastern",
                        "operatingHours":[{"dayOfWeek":"MONDAY","openTime":"09:00","closeTime":"17:00"}]
                    }
                """.trimIndent())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.state").exists())
    }

    @Test
    fun `update location with invalid email returns 400`() {
        val locationId = UUID.randomUUID()
        mockMvc.perform(
            put("/api/v1/admin/locations/$locationId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"not-an-email"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.email").exists())
    }
}
