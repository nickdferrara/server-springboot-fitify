package com.nickdferrara.fitify.logging.internal

import com.nickdferrara.fitify.logging.Audit
import com.nickdferrara.fitify.logging.internal.aspect.AuditAspect
import com.nickdferrara.fitify.logging.internal.config.LoggingProperties
import com.nickdferrara.fitify.logging.internal.service.AuditLogService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import java.util.UUID

class AuditAspectTest {

    private val auditLogService = mockk<AuditLogService>(relaxed = true)
    private val properties = LoggingProperties()
    private lateinit var auditAspect: AuditAspect

    @BeforeEach
    fun setup() {
        auditAspect = AuditAspect(auditLogService, properties)
        MDC.clear()
    }

    private fun createJoinPoint(
        args: Array<Any?> = emptyArray(),
        result: Any? = "result",
    ): ProceedingJoinPoint {
        val joinPoint = mockk<ProceedingJoinPoint>()
        every { joinPoint.args } returns args
        every { joinPoint.proceed() } returns result
        return joinPoint
    }

    @Test
    fun `audit annotation triggers audit record creation`() {
        MDC.put("userId", "user-123")
        MDC.put("module", "coaching")

        val resourceId = UUID.randomUUID()
        val joinPoint = createJoinPoint(args = arrayOf(resourceId))
        val audit = mockk<Audit>()
        every { audit.action } returns "COACH_CREATED"
        every { audit.resourceType } returns "Coach"
        every { audit.includeResult } returns false

        val result = auditAspect.auditMethod(joinPoint, audit)

        assertEquals("result", result)
        verify {
            auditLogService.save(
                userId = "user-123",
                action = "COACH_CREATED",
                module = "coaching",
                resourceType = "Coach",
                resourceId = resourceId.toString(),
                details = null,
                ipAddress = any(),
            )
        }
    }

    @Test
    fun `extracts resource ID from UUID argument`() {
        MDC.put("userId", "user-1")
        MDC.put("module", "scheduling")

        val uuid = UUID.randomUUID()
        val joinPoint = createJoinPoint(args = arrayOf(uuid))
        val audit = mockk<Audit>()
        every { audit.action } returns "CLASS_CANCELLED"
        every { audit.resourceType } returns "FitnessClass"
        every { audit.includeResult } returns false

        auditAspect.auditMethod(joinPoint, audit)

        verify {
            auditLogService.save(
                userId = any(),
                action = any(),
                module = any(),
                resourceType = any(),
                resourceId = uuid.toString(),
                details = any(),
                ipAddress = any(),
            )
        }
    }

    @Test
    fun `skips audit when disabled`() {
        val disabledProperties = LoggingProperties(
            audit = LoggingProperties.AuditProperties(enabled = false),
        )
        val aspect = AuditAspect(auditLogService, disabledProperties)
        val joinPoint = createJoinPoint()
        val audit = mockk<Audit>()

        aspect.auditMethod(joinPoint, audit)

        verify(exactly = 0) { auditLogService.save(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `includes result when includeResult is true`() {
        MDC.put("userId", "user-1")
        MDC.put("module", "test")

        val joinPoint = createJoinPoint(result = "some-result-data")
        val audit = mockk<Audit>()
        every { audit.action } returns "ACTION"
        every { audit.resourceType } returns "Resource"
        every { audit.includeResult } returns true

        auditAspect.auditMethod(joinPoint, audit)

        verify {
            auditLogService.save(
                userId = any(),
                action = any(),
                module = any(),
                resourceType = any(),
                resourceId = any(),
                details = "some-result-data",
                ipAddress = any(),
            )
        }
    }

    @Test
    fun `uses anonymous when userId not in MDC`() {
        val joinPoint = createJoinPoint()
        val audit = mockk<Audit>()
        every { audit.action } returns "ACTION"
        every { audit.resourceType } returns "Resource"
        every { audit.includeResult } returns false

        auditAspect.auditMethod(joinPoint, audit)

        verify {
            auditLogService.save(
                userId = "anonymous",
                action = any(),
                module = any(),
                resourceType = any(),
                resourceId = any(),
                details = any(),
                ipAddress = any(),
            )
        }
    }
}
