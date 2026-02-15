package com.nickdferrara.fitify.logging.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("fitify.logging")
internal data class LoggingProperties(
    val correlationHeader: String = "X-Correlation-ID",
    val aop: AopProperties = AopProperties(),
    val audit: AuditProperties = AuditProperties(),
) {
    data class AopProperties(
        val controllerLogging: Boolean = true,
        val serviceLogging: Boolean = true,
        val maskSensitiveFields: Boolean = true,
    )

    data class AuditProperties(
        val enabled: Boolean = true,
        val retentionDays: Int = 90,
        val async: Boolean = true,
    )
}
