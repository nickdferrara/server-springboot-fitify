package com.nickdferrara.fitify.notification.internal

import com.nickdferrara.fitify.notification.internal.controller.DeviceTokenController
import com.nickdferrara.fitify.notification.internal.dtos.request.RegisterDeviceTokenRequest
import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.notification.internal.entities.NotificationLog
import com.nickdferrara.fitify.notification.internal.entities.NotificationStatus
import com.nickdferrara.fitify.notification.internal.exception.DeviceTokenNotFoundException
import com.nickdferrara.fitify.notification.internal.service.NotificationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID

class DeviceTokenControllerTest {

    private val notificationService = mockk<NotificationService>(relaxed = true)
    private val controller = DeviceTokenController(notificationService)

    private val userId: UUID = UUID.randomUUID()

    private fun buildJwt(): Jwt {
        return Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .subject(userId.toString())
            .build()
    }

    @Test
    fun `registerDeviceToken returns 201`() {
        val request = RegisterDeviceTokenRequest("fcm-token-123", "ANDROID")

        val response = controller.registerDeviceToken(request, buildJwt())

        assertEquals(HttpStatus.CREATED, response.statusCode)
        verify { notificationService.registerDeviceToken(userId, "fcm-token-123", "ANDROID") }
    }

    @Test
    fun `removeDeviceToken returns 204`() {
        val response = controller.removeDeviceToken("fcm-token-123", buildJwt())

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify { notificationService.removeDeviceToken(userId, "fcm-token-123") }
    }

    @Test
    fun `removeDeviceToken throws when token not found`() {
        every { notificationService.removeDeviceToken(userId, "unknown") } throws
            DeviceTokenNotFoundException("unknown")

        assertThrows<DeviceTokenNotFoundException> {
            controller.removeDeviceToken("unknown", buildJwt())
        }
    }

    @Test
    fun `getNotificationHistory returns 200 with list`() {
        val log = NotificationLog(
            id = UUID.randomUUID(),
            userId = userId,
            channel = NotificationChannel.EMAIL,
            eventType = "TestEvent",
            payload = "{}",
            status = NotificationStatus.SENT,
            sentAt = Instant.now(),
        )
        every { notificationService.getNotificationHistory(userId) } returns listOf(log)

        val response = controller.getNotificationHistory(buildJwt())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(1, response.body!!.size)
        assertEquals(userId, response.body!![0].userId)
    }

    @Test
    fun `getNotificationHistory returns empty list when no history`() {
        every { notificationService.getNotificationHistory(userId) } returns emptyList()

        val response = controller.getNotificationHistory(buildJwt())

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body!!.size)
    }
}
