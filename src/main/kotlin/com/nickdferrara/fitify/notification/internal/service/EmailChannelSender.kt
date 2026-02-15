package com.nickdferrara.fitify.notification.internal.service

import com.nickdferrara.fitify.notification.internal.config.NotificationProperties
import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import org.springframework.stereotype.Component
import java.util.UUID

@Component
internal class EmailChannelSender(
    private val emailTransport: EmailTransport,
    private val notificationProperties: NotificationProperties,
) : NotificationChannelSender {

    override fun send(userId: UUID, subject: String, body: String, payload: Map<String, String>) {
        val recipientEmail = payload["email"] ?: return

        val from = EmailAddress(
            email = notificationProperties.fromEmail,
            name = notificationProperties.fromName,
        )
        emailTransport.send(from, recipientEmail, subject, body)
    }

    override fun supports(channel: NotificationChannel): Boolean =
        channel == NotificationChannel.EMAIL
}
