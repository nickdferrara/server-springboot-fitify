package com.nickdferrara.fitify.notification.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fitify.notification")
internal data class NotificationProperties(
    val fromEmail: String,
    val fromName: String,
    val email: EmailProperties = EmailProperties(),
) {
    internal data class EmailProperties(
        val provider: String = "smtp",
    )
}
