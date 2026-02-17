package com.nickdferrara.fitify.notification.internal

import com.nickdferrara.fitify.notification.NotificationFailedEvent
import com.nickdferrara.fitify.notification.NotificationSentEvent
import com.nickdferrara.fitify.notification.internal.entities.DeviceToken
import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.notification.internal.entities.NotificationLog
import com.nickdferrara.fitify.notification.internal.entities.NotificationStatus
import com.nickdferrara.fitify.notification.internal.exception.DeviceTokenNotFoundException
import com.nickdferrara.fitify.notification.internal.exception.NotificationDeliveryException
import com.nickdferrara.fitify.notification.internal.repository.DeviceTokenRepository
import com.nickdferrara.fitify.notification.internal.repository.NotificationLogRepository
import com.nickdferrara.fitify.notification.internal.adapter.interfaces.NotificationChannelSender
import com.nickdferrara.fitify.notification.internal.factory.NotificationPayload
import com.nickdferrara.fitify.notification.internal.factory.NotificationPayloadFactory
import com.nickdferrara.fitify.notification.internal.service.NotificationServiceImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant
import java.util.UUID

class NotificationServiceTest {

    private val notificationLogRepository = mockk<NotificationLogRepository>()
    private val deviceTokenRepository = mockk<DeviceTokenRepository>()
    private val emailSender = mockk<NotificationChannelSender>(relaxed = true)
    private val pushSender = mockk<NotificationChannelSender>(relaxed = true)
    private val payloadFactory = mockk<NotificationPayloadFactory>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val service = NotificationServiceImpl(
        notificationLogRepository,
        deviceTokenRepository,
        listOf(emailSender, pushSender),
        payloadFactory,
        eventPublisher,
    )

    private val userId: UUID = UUID.randomUUID()

    // --- sendNotification Tests ---

    @Test
    fun `sendNotification dispatches to correct channel and logs success`() {
        val logId = UUID.randomUUID()
        every { emailSender.supports(NotificationChannel.EMAIL) } returns true
        every { emailSender.supports(NotificationChannel.PUSH) } returns false
        every { notificationLogRepository.save(any()) } answers {
            val log = firstArg<NotificationLog>()
            NotificationLog(
                id = logId,
                userId = log.userId,
                channel = log.channel,
                eventType = log.eventType,
                payload = log.payload,
                status = log.status,
                sentAt = Instant.now(),
            )
        }

        val payload = NotificationPayload(
            subject = "Test Subject",
            body = "Test Body",
            channels = setOf(NotificationChannel.EMAIL),
            data = mapOf("email" to "test@example.com"),
        )

        service.sendNotification(userId, "TestEvent", payload)

        verify { emailSender.send(userId, "Test Subject", "Test Body", payload.data) }
        val eventSlot = slot<NotificationSentEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(logId, eventSlot.captured.notificationId)
        assertEquals("EMAIL", eventSlot.captured.channel)
    }

    @Test
    fun `sendNotification logs failure and publishes failed event on exception`() {
        val logId = UUID.randomUUID()
        every { emailSender.supports(NotificationChannel.EMAIL) } returns true
        every { emailSender.send(any(), any(), any(), any()) } throws
            NotificationDeliveryException("SMTP error")
        every { notificationLogRepository.save(any()) } answers {
            val log = firstArg<NotificationLog>()
            NotificationLog(
                id = logId,
                userId = log.userId,
                channel = log.channel,
                eventType = log.eventType,
                payload = log.payload,
                status = log.status,
                sentAt = Instant.now(),
            )
        }

        val payload = NotificationPayload(
            subject = "Test",
            body = "Test",
            channels = setOf(NotificationChannel.EMAIL),
            data = mapOf("email" to "test@example.com"),
        )

        service.sendNotification(userId, "TestEvent", payload)

        val eventSlot = slot<NotificationFailedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(logId, eventSlot.captured.notificationId)
        assertEquals("SMTP error", eventSlot.captured.errorMessage)
    }

    @Test
    fun `sendNotification dispatches to multiple channels`() {
        val logId = UUID.randomUUID()
        every { emailSender.supports(NotificationChannel.EMAIL) } returns true
        every { emailSender.supports(NotificationChannel.PUSH) } returns false
        every { pushSender.supports(NotificationChannel.PUSH) } returns true
        every { pushSender.supports(NotificationChannel.EMAIL) } returns false
        every { notificationLogRepository.save(any()) } answers {
            val log = firstArg<NotificationLog>()
            NotificationLog(
                id = logId,
                userId = log.userId,
                channel = log.channel,
                eventType = log.eventType,
                payload = log.payload,
                status = log.status,
                sentAt = Instant.now(),
            )
        }

        val payload = NotificationPayload(
            subject = "Multi",
            body = "Multi Body",
            channels = setOf(NotificationChannel.EMAIL, NotificationChannel.PUSH),
        )

        service.sendNotification(userId, "MultiEvent", payload)

        verify { emailSender.send(userId, "Multi", "Multi Body", any()) }
        verify { pushSender.send(userId, "Multi", "Multi Body", any()) }
    }

    // --- registerDeviceToken Tests ---

    @Test
    fun `registerDeviceToken creates new token when not existing`() {
        every { deviceTokenRepository.findByFcmToken("token123") } returns null
        every { deviceTokenRepository.save(any()) } answers { firstArg() }

        service.registerDeviceToken(userId, "token123", "ANDROID")

        verify {
            deviceTokenRepository.save(match<DeviceToken> {
                it.userId == userId && it.fcmToken == "token123" && it.deviceType == "ANDROID"
            })
        }
    }

    @Test
    fun `registerDeviceToken updates lastUsedAt when token already exists`() {
        val existing = DeviceToken(
            id = UUID.randomUUID(),
            userId = userId,
            fcmToken = "token123",
            deviceType = "ANDROID",
            lastUsedAt = Instant.now().minusSeconds(3600),
        )
        every { deviceTokenRepository.findByFcmToken("token123") } returns existing
        every { deviceTokenRepository.save(any()) } answers { firstArg() }

        service.registerDeviceToken(userId, "token123", "ANDROID")

        verify { deviceTokenRepository.save(existing) }
    }

    // --- removeDeviceToken Tests ---

    @Test
    fun `removeDeviceToken deletes existing token`() {
        val token = DeviceToken(
            id = UUID.randomUUID(),
            userId = userId,
            fcmToken = "token123",
            deviceType = "ANDROID",
        )
        every { deviceTokenRepository.findByFcmToken("token123") } returns token
        every { deviceTokenRepository.delete(token) } returns Unit

        service.removeDeviceToken(userId, "token123")

        verify { deviceTokenRepository.delete(token) }
    }

    @Test
    fun `removeDeviceToken throws when token not found`() {
        every { deviceTokenRepository.findByFcmToken("unknown") } returns null

        assertThrows<DeviceTokenNotFoundException> {
            service.removeDeviceToken(userId, "unknown")
        }
    }

    // --- getNotificationHistory Tests ---

    @Test
    fun `getNotificationHistory returns logs for user`() {
        val log = NotificationLog(
            id = UUID.randomUUID(),
            userId = userId,
            channel = NotificationChannel.EMAIL,
            eventType = "TestEvent",
            payload = "{}",
            status = NotificationStatus.SENT,
            sentAt = Instant.now(),
        )
        every { notificationLogRepository.findByUserId(userId) } returns listOf(log)

        val result = service.getNotificationHistory(userId)

        assertEquals(1, result.size)
        assertEquals(userId, result[0].userId)
    }
}
