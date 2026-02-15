package com.nickdferrara.fitify.scheduling.internal.controller

import com.nickdferrara.fitify.TestSecurityConfig
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ClassResponse
import com.nickdferrara.fitify.scheduling.internal.model.BookClassResult
import com.nickdferrara.fitify.scheduling.internal.service.SchedulingService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(ClassController::class)
@Import(TestSecurityConfig::class)
@EnabledIf("isDockerAvailable")
internal class ClassControllerWebMvcTest {

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

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var schedulingService: SchedulingService

    private val classId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private fun buildClassResponse() = ClassResponse(
        id = classId,
        locationId = UUID.randomUUID(),
        name = "Morning Yoga",
        description = "A relaxing yoga session",
        classType = "yoga",
        coachId = UUID.randomUUID(),
        room = "Studio A",
        startTime = Instant.parse("2025-06-01T09:00:00Z"),
        endTime = Instant.parse("2025-06-01T10:00:00Z"),
        capacity = 20,
        status = "ACTIVE",
        enrolledCount = 5,
        waitlistSize = 0,
        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
    )

    @Test
    fun `GET classes returns paginated results`() {
        val response = buildClassResponse()
        every { schedulingService.searchClasses(any(), any(), any(), any(), any(), any()) } returns
            PageImpl(listOf(response))

        mockMvc.perform(
            get("/api/v1/classes")
                .with(jwt().jwt { it.subject(userId.toString()) })
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].name").value("Morning Yoga"))
            .andExpect(jsonPath("$.content[0].classType").value("yoga"))
    }

    @Test
    fun `POST book class returns CREATED`() {
        val bookingResponse = com.nickdferrara.fitify.scheduling.internal.dtos.response.BookingResponse(
            id = UUID.randomUUID(),
            userId = userId,
            classId = classId,
            className = "Morning Yoga",
            startTime = Instant.parse("2025-06-01T09:00:00Z"),
            status = "CONFIRMED",
            bookedAt = Instant.now(),
        )
        every { schedulingService.bookClass(classId, any()) } returns BookClassResult.Booked(bookingResponse)

        mockMvc.perform(
            post("/api/v1/classes/$classId/book")
                .with(jwt().jwt { it.subject(userId.toString()) })
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `DELETE booking returns NO_CONTENT`() {
        every { schedulingService.cancelBooking(classId, any()) } returns Unit

        mockMvc.perform(
            delete("/api/v1/classes/$classId/booking")
                .with(jwt().jwt { it.subject(userId.toString()) })
        )
            .andExpect(status().isNoContent)

        verify { schedulingService.cancelBooking(classId, any()) }
    }
}
