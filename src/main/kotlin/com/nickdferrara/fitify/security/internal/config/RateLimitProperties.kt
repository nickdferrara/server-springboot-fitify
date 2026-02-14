package com.nickdferrara.fitify.security.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fitify.rate-limit")
internal data class RateLimitProperties(
    val authRequestsPerMinute: Int = 5,
    val passwordResetPerHour: Int = 3,
    val generalRequestsPerMinute: Int = 100,
    val adminRequestsPerMinute: Int = 200,
)
