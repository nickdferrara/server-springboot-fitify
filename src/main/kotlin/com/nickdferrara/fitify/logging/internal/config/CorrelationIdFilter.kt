package com.nickdferrara.fitify.logging.internal.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Base64
import java.util.UUID

internal class CorrelationIdFilter(
    private val properties: LoggingProperties,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val correlationId = request.getHeader(properties.correlationHeader)
                ?: UUID.randomUUID().toString()
            MDC.put("correlationId", correlationId)

            val userId = extractUserIdFromJwt(request)
            if (userId != null) {
                MDC.put("userId", userId)
            }

            val module = extractModuleFromPath(request.requestURI)
            if (module != null) {
                MDC.put("module", module)
            }

            response.setHeader(properties.correlationHeader, correlationId)

            filterChain.doFilter(request, response)
        } finally {
            MDC.remove("correlationId")
            MDC.remove("userId")
            MDC.remove("module")
        }
    }

    private fun extractUserIdFromJwt(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization") ?: return null
        if (!authHeader.startsWith("Bearer ")) return null

        return try {
            val token = authHeader.substringAfter("Bearer ")
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val subMatch = Regex(""""sub"\s*:\s*"([^"]+)"""").find(payload)
            subMatch?.groupValues?.get(1)
        } catch (_: Exception) {
            null
        }
    }

    private fun extractModuleFromPath(path: String): String? {
        val segments = path.removePrefix("/api/v1/").split("/")
        return when {
            segments.isEmpty() -> null
            segments[0] == "admin" && segments.size > 1 -> segments[1]
            else -> segments[0]
        }
    }
}
