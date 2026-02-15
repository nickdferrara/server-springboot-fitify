package com.nickdferrara.fitify.notification.internal.service

import com.nickdferrara.fitify.notification.internal.adapter.interfaces.NotificationChannelSender
import com.nickdferrara.fitify.notification.internal.factory.NotificationPayload
import com.nickdferrara.fitify.notification.internal.factory.NotificationPayloadFactory
import com.nickdferrara.fitify.notification.NotificationApi
import com.nickdferrara.fitify.notification.NotificationFailedEvent
import com.nickdferrara.fitify.notification.NotificationSentEvent
import com.nickdferrara.fitify.notification.internal.entities.DeviceToken
import com.nickdferrara.fitify.notification.internal.entities.NotificationLog
import com.nickdferrara.fitify.notification.internal.entities.NotificationStatus
import com.nickdferrara.fitify.notification.internal.exception.DeviceTokenNotFoundException
import com.nickdferrara.fitify.notification.internal.repository.DeviceTokenRepository
import com.nickdferrara.fitify.notification.internal.repository.NotificationLogRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
internal class NotificationService(
    private val notificationLogRepository: NotificationLogRepository,
    private val deviceTokenRepository: DeviceTokenRepository,
    private val channelSenders: List<NotificationChannelSender>,
    private val payloadFactory: NotificationPayloadFactory,
    private val eventPublisher: ApplicationEventPublisher,
) : NotificationApi {

    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    @Async("notificationExecutor")
    fun sendNotification(userId: UUID, eventType: String, payload: NotificationPayload) {
        for (channel in payload.channels) {
            val sender = channelSenders.find { it.supports(channel) }
            if (sender == null) {
                logger.warn("No sender found for channel {}", channel)
                continue
            }

            val log = notificationLogRepository.save(
                NotificationLog(
                    userId = userId,
                    channel = channel,
                    eventType = eventType,
                    payload = payload.data.toString(),
                    status = NotificationStatus.SENT,
                ),
            )

            try {
                sender.send(userId, payload.subject, payload.body, payload.data)
                eventPublisher.publishEvent(
                    NotificationSentEvent(
                        notificationId = log.id!!,
                        channel = channel.name,
                        status = NotificationStatus.SENT.name,
                    ),
                )
            } catch (ex: Exception) {
                log.status = NotificationStatus.FAILED
                notificationLogRepository.save(log)
                logger.error("Failed to send {} notification to user {}: {}", channel, userId, ex.message)
                eventPublisher.publishEvent(
                    NotificationFailedEvent(
                        notificationId = log.id!!,
                        channel = channel.name,
                        errorMessage = ex.message ?: "Unknown error",
                    ),
                )
            }
        }
    }

    override fun registerDeviceToken(userId: UUID, token: String, deviceType: String) {
        val existing = deviceTokenRepository.findByFcmToken(token)
        if (existing != null) {
            existing.lastUsedAt = Instant.now()
            deviceTokenRepository.save(existing)
            return
        }

        deviceTokenRepository.save(
            DeviceToken(
                userId = userId,
                fcmToken = token,
                deviceType = deviceType,
                lastUsedAt = Instant.now(),
            ),
        )
    }

    override fun removeDeviceToken(userId: UUID, token: String) {
        val deviceToken = deviceTokenRepository.findByFcmToken(token)
            ?: throw DeviceTokenNotFoundException(token)
        deviceTokenRepository.delete(deviceToken)
    }

    fun getNotificationHistory(userId: UUID) =
        notificationLogRepository.findByUserId(userId)
}
