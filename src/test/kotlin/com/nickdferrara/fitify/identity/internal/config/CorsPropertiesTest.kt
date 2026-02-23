package com.nickdferrara.fitify.identity.internal.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CorsPropertiesTest {

    @Test
    fun `defaults are correct`() {
        val props = CorsProperties()
        assertThat(props.allowedOrigins).containsExactly("http://localhost:3000")
        assertThat(props.allowedMethods).containsExactly("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        assertThat(props.allowedHeaders).containsExactly("*")
        assertThat(props.allowCredentials).isTrue()
        assertThat(props.maxAge).isEqualTo(3600)
    }

    @Test
    fun `custom values override defaults`() {
        val props = CorsProperties(
            allowedOrigins = listOf("https://app.example.com", "https://admin.example.com"),
            allowedMethods = listOf("GET", "POST"),
            allowedHeaders = listOf("Authorization", "Content-Type"),
            allowCredentials = false,
            maxAge = 7200,
        )
        assertThat(props.allowedOrigins).containsExactly("https://app.example.com", "https://admin.example.com")
        assertThat(props.allowedMethods).containsExactly("GET", "POST")
        assertThat(props.allowedHeaders).containsExactly("Authorization", "Content-Type")
        assertThat(props.allowCredentials).isFalse()
        assertThat(props.maxAge).isEqualTo(7200)
    }
}
