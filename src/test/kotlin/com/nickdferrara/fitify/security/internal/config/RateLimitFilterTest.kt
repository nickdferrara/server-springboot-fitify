package com.nickdferrara.fitify.security.internal.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import io.mockk.mockk
import io.mockk.verify

class RateLimitFilterTest {

    private val objectMapper = ObjectMapper()
    private val properties = RateLimitProperties(
        authRequestsPerMinute = 5,
        passwordResetPerHour = 3,
        generalRequestsPerMinute = 100,
        adminRequestsPerMinute = 200,
    )
    private lateinit var filter: RateLimitFilter
    private val filterChain = mockk<FilterChain>(relaxed = true)

    @BeforeEach
    fun setup() {
        filter = RateLimitFilter(properties, objectMapper)
    }

    private fun createRequest(path: String, method: String = "GET", ip: String = "127.0.0.1"): MockHttpServletRequest {
        val request = MockHttpServletRequest(method, path)
        request.remoteAddr = ip
        return request
    }

    @Test
    fun `auth endpoint returns 429 after exceeding limit`() {
        for (i in 1..5) {
            val request = createRequest("/api/v1/auth/login", "POST")
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, filterChain)
            assertEquals(HttpStatus.OK.value(), response.status)
        }

        val request = createRequest("/api/v1/auth/login", "POST")
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, filterChain)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
    }

    @Test
    fun `password reset endpoint returns 429 after 3 requests`() {
        for (i in 1..3) {
            val request = createRequest("/api/v1/auth/forgot-password", "POST")
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, filterChain)
            assertEquals(HttpStatus.OK.value(), response.status)
        }

        val request = createRequest("/api/v1/auth/forgot-password", "POST")
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, filterChain)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
    }

    @Test
    fun `webhook endpoints are not rate limited`() {
        for (i in 1..300) {
            val request = createRequest("/api/v1/webhooks/stripe", "POST")
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, filterChain)
            assertEquals(HttpStatus.OK.value(), response.status)
        }
    }

    @Test
    fun `actuator endpoints are not rate limited`() {
        for (i in 1..300) {
            val request = createRequest("/actuator/health", "GET")
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, filterChain)
            assertEquals(HttpStatus.OK.value(), response.status)
        }
    }

    @Test
    fun `different IPs get independent buckets`() {
        for (i in 1..5) {
            val request = createRequest("/api/v1/auth/login", "POST", "10.0.0.1")
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, filterChain)
            assertEquals(HttpStatus.OK.value(), response.status)
        }

        // First IP should be rate limited
        val limitedRequest = createRequest("/api/v1/auth/login", "POST", "10.0.0.1")
        val limitedResponse = MockHttpServletResponse()
        filter.doFilter(limitedRequest, limitedResponse, filterChain)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), limitedResponse.status)

        // Second IP should still work
        val freshRequest = createRequest("/api/v1/auth/login", "POST", "10.0.0.2")
        val freshResponse = MockHttpServletResponse()
        filter.doFilter(freshRequest, freshResponse, filterChain)
        assertEquals(HttpStatus.OK.value(), freshResponse.status)
    }

    @Test
    fun `rate limited response includes Retry-After header`() {
        for (i in 1..5) {
            val request = createRequest("/api/v1/auth/login", "POST")
            filter.doFilter(request, MockHttpServletResponse(), filterChain)
        }

        val request = createRequest("/api/v1/auth/login", "POST")
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, filterChain)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
        val retryAfter = response.getHeader("Retry-After")
        assertEquals(true, retryAfter != null && retryAfter.toLong() > 0)
    }

    @Test
    fun `admin endpoints use IP for bucketing`() {
        for (i in 1..200) {
            val request = createRequest("/api/v1/admin/locations", "GET")
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, filterChain)
            assertEquals(HttpStatus.OK.value(), response.status)
        }

        val request = createRequest("/api/v1/admin/locations", "GET")
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, filterChain)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
    }

    @Test
    fun `general endpoints are rate limited at 100 per minute`() {
        for (i in 1..100) {
            val request = createRequest("/api/v1/locations", "GET")
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, filterChain)
            assertEquals(HttpStatus.OK.value(), response.status)
        }

        val request = createRequest("/api/v1/locations", "GET")
        val response = MockHttpServletResponse()
        filter.doFilter(request, response, filterChain)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
    }

    @Test
    fun `forged JWT falls back to IP-based bucketing`() {
        val forgedToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJmb3JnZWQtdXNlciJ9.fakesignature"

        // Send request with forged JWT
        val requestWithJwt = createRequest("/api/v1/locations", "GET", "192.168.1.1")
        requestWithJwt.addHeader("Authorization", "Bearer $forgedToken")
        val responseWithJwt = MockHttpServletResponse()
        filter.doFilter(requestWithJwt, responseWithJwt, filterChain)
        assertEquals(HttpStatus.OK.value(), responseWithJwt.status)

        // Send request without JWT from same IP — should share the same bucket
        val requestWithoutJwt = createRequest("/api/v1/locations", "GET", "192.168.1.1")
        val responseWithoutJwt = MockHttpServletResponse()
        filter.doFilter(requestWithoutJwt, responseWithoutJwt, filterChain)
        assertEquals(HttpStatus.OK.value(), responseWithoutJwt.status)

        // Exhaust remaining tokens (100 total, 2 used)
        for (i in 3..100) {
            val request = createRequest("/api/v1/locations", "GET", "192.168.1.1")
            filter.doFilter(request, MockHttpServletResponse(), filterChain)
        }

        // Both forged-JWT and no-JWT requests from same IP should now be blocked
        val blockedRequest = createRequest("/api/v1/locations", "GET", "192.168.1.1")
        blockedRequest.addHeader("Authorization", "Bearer $forgedToken")
        val blockedResponse = MockHttpServletResponse()
        filter.doFilter(blockedRequest, blockedResponse, filterChain)
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), blockedResponse.status)
    }
}
