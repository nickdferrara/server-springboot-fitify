package com.nickdferrara.fitify.subscription.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fitify.stripe")
internal data class StripeProperties(
    val secretKey: String,
    val webhookSecret: String,
    val successUrl: String,
    val cancelUrl: String,
)
