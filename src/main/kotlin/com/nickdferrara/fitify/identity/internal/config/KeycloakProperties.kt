package com.nickdferrara.fitify.identity.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fitify.keycloak")
internal data class KeycloakProperties(
    val serverUrl: String,
    val realm: String,
    val clientId: String,
    val clientSecret: String,
)
