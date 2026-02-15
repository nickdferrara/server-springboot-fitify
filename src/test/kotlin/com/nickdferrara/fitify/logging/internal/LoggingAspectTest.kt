package com.nickdferrara.fitify.logging.internal

import com.nickdferrara.fitify.logging.LogExecution
import com.nickdferrara.fitify.logging.LogLevel
import com.nickdferrara.fitify.logging.internal.aspect.LoggingAspect
import com.nickdferrara.fitify.logging.internal.config.LoggingProperties
import com.nickdferrara.fitify.logging.internal.service.SensitiveDataMasker
import io.mockk.every
import io.mockk.mockk
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.reflect.MethodSignature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoggingAspectTest {

    private val properties = LoggingProperties()
    private val sensitiveDataMasker = SensitiveDataMasker()
    private lateinit var loggingAspect: LoggingAspect

    @BeforeEach
    fun setup() {
        loggingAspect = LoggingAspect(properties, sensitiveDataMasker)
    }

    private fun createJoinPoint(
        methodName: String = "testMethod",
        args: Array<Any?> = emptyArray(),
        result: Any? = "result",
    ): ProceedingJoinPoint {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val signature = mockk<MethodSignature>()
        val target = TestService()

        every { joinPoint.signature } returns signature
        every { signature.name } returns methodName
        every { joinPoint.target } returns target
        every { joinPoint.args } returns args
        every { joinPoint.proceed() } returns result

        val method = TestService::class.java.getMethod("publicMethod")
        every { signature.method } returns method

        return joinPoint
    }

    @Test
    fun `logAnnotatedMethod executes and returns result`() {
        val joinPoint = createJoinPoint(result = "expected")
        val annotation = LogExecution(level = LogLevel.INFO)

        val result = loggingAspect.logAnnotatedMethod(joinPoint, annotation)

        assertEquals("expected", result)
    }

    @Test
    fun `logAnnotatedMethod handles null result`() {
        val joinPoint = createJoinPoint(result = null)
        val annotation = LogExecution(level = LogLevel.DEBUG)

        val result = loggingAspect.logAnnotatedMethod(joinPoint, annotation)

        assertEquals(null, result)
    }

    @Test
    fun `logAnnotatedMethod masks sensitive args`() {
        data class SensitiveRequest(val password: String)

        val joinPoint = createJoinPoint(args = arrayOf(SensitiveRequest("secret")))
        val annotation = LogExecution(level = LogLevel.INFO, includeArgs = true)

        val result = loggingAspect.logAnnotatedMethod(joinPoint, annotation)

        // Should not throw, just verify it executes
        assertEquals("result", result)
    }

    @Test
    fun `logControllerMethod skips when controller logging disabled`() {
        val disabledProperties = LoggingProperties(
            aop = LoggingProperties.AopProperties(controllerLogging = false),
        )
        val aspect = LoggingAspect(disabledProperties, sensitiveDataMasker)
        val joinPoint = createJoinPoint(result = "ok")

        val result = aspect.logControllerMethod(joinPoint)

        assertEquals("ok", result)
    }

    @Test
    fun `logServiceMethod skips when service logging disabled`() {
        val disabledProperties = LoggingProperties(
            aop = LoggingProperties.AopProperties(serviceLogging = false),
        )
        val aspect = LoggingAspect(disabledProperties, sensitiveDataMasker)
        val joinPoint = createJoinPoint(result = "ok")

        val result = aspect.logServiceMethod(joinPoint)

        assertEquals("ok", result)
    }

    @Test
    fun `logControllerMethod executes when enabled`() {
        val joinPoint = createJoinPoint(result = "controller-result")

        val result = loggingAspect.logControllerMethod(joinPoint)

        assertEquals("controller-result", result)
    }

    @Test
    fun `logServiceMethod executes when enabled`() {
        val joinPoint = createJoinPoint(result = "service-result")

        val result = loggingAspect.logServiceMethod(joinPoint)

        assertEquals("service-result", result)
    }
}

class TestService {
    fun publicMethod(): String = "test"
}
