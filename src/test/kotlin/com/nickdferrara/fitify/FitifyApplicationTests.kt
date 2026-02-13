package com.nickdferrara.fitify

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@EnabledIf("isDockerAvailable")
class FitifyApplicationTests {

    companion object {
        @JvmStatic
        fun isDockerAvailable(): Boolean {
            return try {
                org.testcontainers.DockerClientFactory.instance().isDockerAvailable
            } catch (_: Exception) {
                false
            }
        }
    }

    @Test
    fun contextLoads() {
    }
}
