package com.nickdferrara.fitify.security.internal.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@EnableConfigurationProperties(RateLimitProperties::class)
internal class RateLimitFilter(
    private val properties: RateLimitProperties,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    private val buckets = ConcurrentHashMap<String, Bucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val path = request.requestURI
        val method = request.method

        if (isExempt(path)) {
            filterChain.doFilter(request, response)
            return
        }

        val bucketKey = resolveBucketKey(path, method, request) ?: run {
            filterChain.doFilter(request, response)
            return
        }

        val bucket = buckets.computeIfAbsent(bucketKey) { createBucket(path, method) }
        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            filterChain.doFilter(request, response)
        } else {
            val retryAfterSeconds = probe.nanosToWaitForRefill / 1_000_000_000 + 1
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.setHeader("Retry-After", retryAfterSeconds.toString())
            response.writer.write(
                objectMapper.writeValueAsString(
                    mapOf("error" to "Too many requests", "message" to "Rate limit exceeded")
                )
            )
        }
    }

    private fun isExempt(path: String): Boolean {
        return path.startsWith("/api/v1/webhooks/") || path.startsWith("/actuator/")
    }

    /**
     * Resolves the client IP from the `X-Forwarded-For` header (leftmost entry),
     * falling back to [HttpServletRequest.getRemoteAddr].
     *
     * Production reverse proxies (nginx, ALB, Cloudflare) should strip or overwrite
     * the `X-Forwarded-For` header from untrusted clients to prevent spoofing.
     */
    private fun resolveClientIp(request: HttpServletRequest): String {
        return request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.ifEmpty { null }
            ?: request.remoteAddr
    }

    private fun resolveBucketKey(path: String, method: String, request: HttpServletRequest): String? {
        val ip = resolveClientIp(request)

        return when {
            path == "/api/v1/auth/forgot-password" && method == "POST" -> "pw-reset:$ip"
            path.startsWith("/api/v1/auth/") -> "auth:$ip"
            path.startsWith("/api/v1/admin/") -> "admin:$ip"
            path.startsWith("/api/v1/") -> "general:$ip"
            else -> null
        }
    }

    private fun createBucket(path: String, method: String): Bucket {
        val bandwidth = when {
            path == "/api/v1/auth/forgot-password" && method == "POST" ->
                Bandwidth.builder()
                    .capacity(properties.passwordResetPerHour.toLong())
                    .refillGreedy(properties.passwordResetPerHour.toLong(), Duration.ofHours(1))
                    .build()
            path.startsWith("/api/v1/auth/") ->
                Bandwidth.builder()
                    .capacity(properties.authRequestsPerMinute.toLong())
                    .refillGreedy(properties.authRequestsPerMinute.toLong(), Duration.ofMinutes(1))
                    .build()
            path.startsWith("/api/v1/admin/") ->
                Bandwidth.builder()
                    .capacity(properties.adminRequestsPerMinute.toLong())
                    .refillGreedy(properties.adminRequestsPerMinute.toLong(), Duration.ofMinutes(1))
                    .build()
            else ->
                Bandwidth.builder()
                    .capacity(properties.generalRequestsPerMinute.toLong())
                    .refillGreedy(properties.generalRequestsPerMinute.toLong(), Duration.ofMinutes(1))
                    .build()
        }

        return Bucket.builder().addLimit(bandwidth).build()
    }

    @Scheduled(fixedRate = 600_000)
    fun cleanupStaleBuckets() {
        val keysToRemove = buckets.entries
            .filter { it.value.availableTokens == it.value.tryConsumeAndReturnRemaining(0).remainingTokens }
            .map { it.key }

        keysToRemove.forEach { buckets.remove(it) }
    }
}
