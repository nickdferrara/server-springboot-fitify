package com.nickdferrara.fitify.notification.internal.service

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.Notification
import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.notification.internal.exception.NotificationDeliveryException
import com.nickdferrara.fitify.notification.internal.repository.DeviceTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
internal class PushChannelSender(
    private val deviceTokenRepository: DeviceTokenRepository,
) : NotificationChannelSender {

    private val logger = LoggerFactory.getLogger(PushChannelSender::class.java)

    override fun send(userId: UUID, subject: String, body: String, payload: Map<String, String>) {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase not initialized, skipping push notification for user {}", userId)
            return
        }

        val tokens = deviceTokenRepository.findByUserId(userId)
        if (tokens.isEmpty()) {
            logger.debug("No device tokens found for user {}", userId)
            return
        }

        for (deviceToken in tokens) {
            try {
                val message = Message.builder()
                    .setToken(deviceToken.fcmToken)
                    .setNotification(
                        Notification.builder()
                            .setTitle(subject)
                            .setBody(body)
                            .build(),
                    )
                    .putAllData(payload)
                    .build()

                FirebaseMessaging.getInstance().send(message)
            } catch (ex: FirebaseMessagingException) {
                if (ex.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                    logger.info("Pruning unregistered token for user {}", userId)
                    deviceTokenRepository.deleteByFcmToken(deviceToken.fcmToken)
                } else {
                    throw NotificationDeliveryException(
                        "Failed to send push to user $userId: ${ex.message}",
                        ex,
                    )
                }
            }
        }
    }

    override fun supports(channel: NotificationChannel): Boolean =
        channel == NotificationChannel.PUSH
}
