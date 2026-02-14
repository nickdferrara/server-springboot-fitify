package com.nickdferrara.fitify.notification.internal.service

import com.nickdferrara.fitify.notification.internal.config.NotificationProperties
import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.notification.internal.exception.NotificationDeliveryException
import jakarta.mail.internet.InternetAddress
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.util.UUID

@Component
internal class EmailChannelSender(
    private val mailSender: JavaMailSender,
    private val notificationProperties: NotificationProperties,
) : NotificationChannelSender {

    override fun send(userId: UUID, subject: String, body: String, payload: Map<String, String>) {
        val recipientEmail = payload["email"] ?: return

        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(InternetAddress(notificationProperties.fromEmail, notificationProperties.fromName))
            helper.setTo(recipientEmail)
            helper.setSubject(subject)
            helper.setText(body, true)
            mailSender.send(message)
        } catch (ex: Exception) {
            throw NotificationDeliveryException("Failed to send email to $recipientEmail", ex)
        }
    }

    override fun supports(channel: NotificationChannel): Boolean =
        channel == NotificationChannel.EMAIL
}
