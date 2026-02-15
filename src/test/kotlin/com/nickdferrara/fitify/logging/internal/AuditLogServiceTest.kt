package com.nickdferrara.fitify.logging.internal

import com.nickdferrara.fitify.logging.internal.entities.AuditLogEntity
import com.nickdferrara.fitify.logging.internal.repository.AuditLogRepository
import com.nickdferrara.fitify.logging.internal.service.AuditLogService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.UUID

class AuditLogServiceTest {

    private val auditLogRepository = mockk<AuditLogRepository>(relaxed = true)
    private val auditLogService = AuditLogService(auditLogRepository)

    @Test
    fun `save persists audit log entity`() {
        val entitySlot = slot<AuditLogEntity>()
        every { auditLogRepository.save(capture(entitySlot)) } answers { firstArg() }

        auditLogService.save(
            userId = "user-123",
            action = "COACH_CREATED",
            module = "coaching",
            resourceType = "Coach",
            resourceId = "abc-123",
            details = """{"name":"John"}""",
            ipAddress = "192.168.1.1",
        )

        verify { auditLogRepository.save(any()) }
        assertEquals("user-123", entitySlot.captured.userId)
        assertEquals("COACH_CREATED", entitySlot.captured.action)
        assertEquals("coaching", entitySlot.captured.module)
        assertEquals("Coach", entitySlot.captured.resourceType)
        assertEquals("abc-123", entitySlot.captured.resourceId)
        assertEquals("""{"name":"John"}""", entitySlot.captured.details)
        assertEquals("192.168.1.1", entitySlot.captured.ipAddress)
    }

    @Test
    fun `queryAuditLogs returns mapped responses`() {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val entity = AuditLogEntity(
            id = id,
            timestamp = now,
            userId = "user-1",
            action = "LOGIN",
            module = "identity",
            resourceType = "User",
            resourceId = "user-1",
            details = null,
            ipAddress = "10.0.0.1",
        )

        every {
            auditLogRepository.findByFilters(
                userId = "user-1",
                action = null,
                module = null,
                from = null,
                to = null,
                pageable = any(),
            )
        } returns PageImpl(listOf(entity))

        val results = auditLogService.queryAuditLogs(userId = "user-1")

        assertEquals(1, results.size)
        assertEquals(id, results[0].id)
        assertEquals("user-1", results[0].userId)
        assertEquals("LOGIN", results[0].action)
        assertEquals("identity", results[0].module)
    }

    @Test
    fun `queryAuditLogs with all filters`() {
        val from = Instant.parse("2025-01-01T00:00:00Z")
        val to = Instant.parse("2025-12-31T23:59:59Z")

        every {
            auditLogRepository.findByFilters(
                userId = "user-1",
                action = "COACH_CREATED",
                module = "coaching",
                from = from,
                to = to,
                pageable = any(),
            )
        } returns PageImpl(emptyList())

        val results = auditLogService.queryAuditLogs(
            userId = "user-1",
            action = "COACH_CREATED",
            module = "coaching",
            from = from,
            to = to,
        )

        assertEquals(0, results.size)
    }

    @Test
    fun `queryAuditLogs with no filters returns all`() {
        every {
            auditLogRepository.findByFilters(null, null, null, null, null, any())
        } returns PageImpl(emptyList())

        val results = auditLogService.queryAuditLogs()

        assertEquals(0, results.size)
        verify {
            auditLogRepository.findByFilters(null, null, null, null, null, any<Pageable>())
        }
    }
}
