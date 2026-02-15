package com.nickdferrara.fitify.notification.internal.service

import com.nickdferrara.fitify.notification.internal.exception.NotificationDeliveryException
import jakarta.mail.internet.InternetAddress
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

internal class SmtpEmailTransportAdapter(
    private val mailSender: JavaMailSender,
) : EmailTransport {

    override fun send(from: EmailAddress, to: String, subject: String, htmlBody: String) {
        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(InternetAddress(from.email, from.name))
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlBody, true)
            mailSender.send(message)
        } catch (ex: Exception) {
            throw NotificationDeliveryException("Failed to send email to $to", ex)
        }
    }
}
