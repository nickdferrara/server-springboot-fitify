package com.nickdferrara.fitify.logging.internal

import com.nickdferrara.fitify.logging.internal.config.CorrelationIdFilter
import com.nickdferrara.fitify.logging.internal.config.LoggingProperties
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.util.Base64

class CorrelationIdFilterTest {

    private val properties = LoggingProperties()
    private lateinit var filter: CorrelationIdFilter
    private val filterChain = mockk<FilterChain>(relaxed = true)

    @BeforeEach
    fun setup() {
        filter = CorrelationIdFilter(properties)
        MDC.clear()
    }

    @Test
    fun `generates new correlation ID when header is missing`() {
        val request = MockHttpServletRequest("GET", "/api/v1/locations")
        val response = MockHttpServletResponse()

        var capturedCorrelationId: String? = null
        val chain = FilterChain { _, _ ->
            capturedCorrelationId = MDC.get("correlationId")
        }

        filter.doFilter(request, response, chain)

        assertNotNull(capturedCorrelationId)
        assertNotNull(response.getHeader("X-Correlation-ID"))
        assertEquals(capturedCorrelationId, response.getHeader("X-Correlation-ID"))
    }

    @Test
    fun `preserves existing correlation ID from header`() {
        val request = MockHttpServletRequest("GET", "/api/v1/locations")
        request.addHeader("X-Correlation-ID", "existing-correlation-id")
        val response = MockHttpServletResponse()

        var capturedCorrelationId: String? = null
        val chain = FilterChain { _, _ ->
            capturedCorrelationId = MDC.get("correlationId")
        }

        filter.doFilter(request, response, chain)

        assertEquals("existing-correlation-id", capturedCorrelationId)
        assertEquals("existing-correlation-id", response.getHeader("X-Correlation-ID"))
    }

    @Test
    fun `extracts userId from JWT`() {
        val request = MockHttpServletRequest("GET", "/api/v1/locations")
        addJwtHeader(request, "user-123")
        val response = MockHttpServletResponse()

        var capturedUserId: String? = null
        val chain = FilterChain { _, _ ->
            capturedUserId = MDC.get("userId")
        }

        filter.doFilter(request, response, chain)

        assertEquals("user-123", capturedUserId)
    }

    @Test
    fun `userId is null when no Authorization header`() {
        val request = MockHttpServletRequest("GET", "/api/v1/locations")
        val response = MockHttpServletResponse()

        var capturedUserId: String? = null
        val chain = FilterChain { _, _ ->
            capturedUserId = MDC.get("userId")
        }

        filter.doFilter(request, response, chain)

        assertNull(capturedUserId)
    }

    @Test
    fun `cleans up MDC after request`() {
        val request = MockHttpServletRequest("GET", "/api/v1/locations")
        request.addHeader("X-Correlation-ID", "test-id")
        addJwtHeader(request, "user-456")
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        assertNull(MDC.get("correlationId"))
        assertNull(MDC.get("userId"))
        assertNull(MDC.get("module"))
    }

    @Test
    fun `extracts module from request path`() {
        val request = MockHttpServletRequest("GET", "/api/v1/locations")
        val response = MockHttpServletResponse()

        var capturedModule: String? = null
        val chain = FilterChain { _, _ ->
            capturedModule = MDC.get("module")
        }

        filter.doFilter(request, response, chain)

        assertEquals("locations", capturedModule)
    }

    @Test
    fun `extracts module from admin path`() {
        val request = MockHttpServletRequest("GET", "/api/v1/admin/coaches")
        val response = MockHttpServletResponse()

        var capturedModule: String? = null
        val chain = FilterChain { _, _ ->
            capturedModule = MDC.get("module")
        }

        filter.doFilter(request, response, chain)

        assertEquals("coaches", capturedModule)
    }

    private fun addJwtHeader(request: MockHttpServletRequest, subject: String) {
        val header = """{"alg":"RS256"}"""
        val payload = """{"sub":"$subject"}"""
        val headerB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(header.toByteArray())
        val payloadB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
        request.addHeader("Authorization", "Bearer $headerB64.$payloadB64.signature")
    }
}
