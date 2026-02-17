package com.nickdferrara.fitify.subscription.internal.controller

import com.nickdferrara.fitify.TestSecurityConfig
import com.nickdferrara.fitify.subscription.internal.service.interfaces.SubscriptionService
import com.ninjasquad.springmockk.MockkBean
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(SubscriptionController::class)
@Import(TestSecurityConfig::class)
internal class SubscriptionControllerValidationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var subscriptionService: SubscriptionService

    @Test
    fun `checkout with invalid planId returns 400`() {
        mockMvc.perform(
            post("/api/v1/subscriptions/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"planId":"not-a-uuid"}""")
                .with(jwt().jwt { it.subject(UUID.randomUUID().toString()) })
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `change plan with invalid newPlanId returns 400`() {
        mockMvc.perform(
            post("/api/v1/subscriptions/me/change-plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"newPlanId":"not-a-uuid"}""")
                .with(jwt().jwt { it.subject(UUID.randomUUID().toString()) })
        )
            .andExpect(status().isBadRequest)
    }
}
