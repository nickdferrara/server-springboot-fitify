package com.nickdferrara.fitify.subscription

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.modulith.test.ApplicationModuleTest

@ApplicationModuleTest
@EnabledIf("isDockerAvailable")
class SubscriptionModuleIntegrationTest {

    companion object {
        @JvmStatic
        fun isDockerAvailable(): Boolean {
            return try {
                val process = ProcessBuilder("docker", "info").start()
                process.waitFor() == 0
            } catch (_: Exception) {
                false
            }
        }
    }

    @Test
    fun `subscription module bootstraps in isolation`() {
        // Module context loads successfully - verified by Spring context initialization
    }
}
