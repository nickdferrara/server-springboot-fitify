package com.nickdferrara.fitify.identity.internal.config

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

internal class CorsConfigTest {

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        val corsProperties = CorsProperties(
            allowedOrigins = listOf("http://localhost:3000", "http://example.com"),
        )
        val corsConfig = CorsConfiguration().apply {
            allowedOrigins = corsProperties.allowedOrigins
            allowedMethods = corsProperties.allowedMethods
            allowedHeaders = corsProperties.allowedHeaders
            allowCredentials = corsProperties.allowCredentials
            maxAge = corsProperties.maxAge
        }
        val corsSource = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfig)
        }
        val builder = MockMvcBuilders.standaloneSetup(TestController())
        builder.addFilter<StandaloneMockMvcBuilder>(CorsFilter(corsSource))
        mockMvc = builder.build()
    }

    @RestController
    class TestController {
        @GetMapping("/api/v1/auth/cors-test")
        fun corsTest() = "ok"
    }

    @Test
    fun `preflight from allowed origin returns CORS headers`() {
        mockMvc.perform(
            options("/api/v1/auth/cors-test")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()),
        )
            .andExpect(status().isOk)
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
            .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
    }

    @Test
    fun `preflight from disallowed origin is rejected`() {
        mockMvc.perform(
            options("/api/v1/auth/cors-test")
                .header(HttpHeaders.ORIGIN, "http://evil.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name()),
        )
            .andExpect(status().isForbidden)
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    @Test
    fun `simple request from allowed origin includes CORS headers`() {
        mockMvc.perform(
            get("/api/v1/auth/cors-test")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000"),
        )
            .andExpect(status().isOk)
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
    }

    @Test
    fun `simple request from disallowed origin is rejected`() {
        mockMvc.perform(
            get("/api/v1/auth/cors-test")
                .header(HttpHeaders.ORIGIN, "http://evil.com"),
        )
            .andExpect(status().isForbidden)
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    @Test
    fun `multiple allowed origins work`() {
        mockMvc.perform(
            get("/api/v1/auth/cors-test")
                .header(HttpHeaders.ORIGIN, "http://example.com"),
        )
            .andExpect(status().isOk)
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://example.com"))
    }
}
