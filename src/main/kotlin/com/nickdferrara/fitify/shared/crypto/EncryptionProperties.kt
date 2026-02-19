package com.nickdferrara.fitify.shared.crypto

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fitify.encryption")
data class EncryptionProperties(
    val key: String,
    val runMigration: Boolean = false,
    val legacyEcbEnabled: Boolean = true,
)
