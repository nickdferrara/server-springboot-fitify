package com.nickdferrara.fitify.notification.internal.controller

import com.nickdferrara.fitify.TestSecurityConfig
import com.nickdferrara.fitify.notification.internal.service.NotificationService
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(DeviceTokenController::class)
@Import(TestSecurityConfig::class)
internal class DeviceTokenControllerValidationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var notificationService: NotificationService

    @Test
    fun `register device token with blank fields returns 400`() {
        mockMvc.perform(
            post("/api/v1/notifications/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"token":"","deviceType":""}""")
                .with(jwt().jwt { it.subject(UUID.randomUUID().toString()) })
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.token").exists())
            .andExpect(jsonPath("$.errors.deviceType").exists())
    }
}
