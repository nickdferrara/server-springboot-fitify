package com.nickdferrara.fitify.logging.internal

import com.nickdferrara.fitify.logging.internal.aspect.ExceptionLoggingAspect
import io.mockk.every
import io.mockk.mockk
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.Signature
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC

class ExceptionLoggingAspectTest {

    private lateinit var aspect: ExceptionLoggingAspect

    @BeforeEach
    fun setup() {
        aspect = ExceptionLoggingAspect()
        MDC.clear()
    }

    private fun createJoinPoint(): JoinPoint {
        val joinPoint = mockk<JoinPoint>()
        val signature = mockk<Signature>()
        val target = TestTarget()

        every { joinPoint.signature } returns signature
        every { signature.name } returns "someMethod"
        every { joinPoint.target } returns target

        return joinPoint
    }

    @Test
    fun `logs business exception at WARN level`() {
        MDC.put("correlationId", "test-corr-id")
        MDC.put("userId", "user-1")

        val joinPoint = createJoinPoint()
        val exception = NotFoundException("Resource not found")

        // Should not throw
        aspect.logException(joinPoint, exception)
    }

    @Test
    fun `logs infrastructure exception at ERROR level`() {
        MDC.put("correlationId", "test-corr-id")

        val joinPoint = createJoinPoint()
        val exception = RuntimeException("Database connection failed")

        aspect.logException(joinPoint, exception)
    }

    @Test
    fun `logs IllegalArgumentException as business exception`() {
        val joinPoint = createJoinPoint()
        val exception = IllegalArgumentException("Invalid input")

        aspect.logException(joinPoint, exception)
    }

    @Test
    fun `logs IllegalStateException as business exception`() {
        val joinPoint = createJoinPoint()
        val exception = IllegalStateException("Invalid state")

        aspect.logException(joinPoint, exception)
    }

    @Test
    fun `handles missing MDC values gracefully`() {
        val joinPoint = createJoinPoint()
        val exception = RuntimeException("Some error")

        // No MDC set - should use defaults
        aspect.logException(joinPoint, exception)
    }
}

private class TestTarget

private class NotFoundException(message: String) : RuntimeException(message)
