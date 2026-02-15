package com.nickdferrara.fitify.notification.internal

import com.nickdferrara.fitify.notification.internal.exception.NotificationDeliveryException
import com.nickdferrara.fitify.notification.internal.service.EmailAddress
import com.nickdferrara.fitify.notification.internal.service.SmtpEmailTransportAdapter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender

class SmtpEmailTransportAdapterTest {

    private val mailSender = mockk<JavaMailSender>()
    private val adapter = SmtpEmailTransportAdapter(mailSender)

    private val from = EmailAddress("noreply@fitify.com", "Fitify")

    @Test
    fun `send creates mime message and delegates to JavaMailSender`() {
        val mimeMessage = mockk<MimeMessage>(relaxed = true)
        every { mailSender.createMimeMessage() } returns mimeMessage
        every { mailSender.send(mimeMessage) } returns Unit

        adapter.send(from, "user@example.com", "Welcome", "<h1>Hello</h1>")

        verify { mailSender.createMimeMessage() }
        verify { mailSender.send(mimeMessage) }
    }

    @Test
    fun `send wraps mail exception in NotificationDeliveryException`() {
        val mimeMessage = mockk<MimeMessage>(relaxed = true)
        every { mailSender.createMimeMessage() } returns mimeMessage
        every { mailSender.send(mimeMessage) } throws MailSendException("SMTP error")

        assertThrows<NotificationDeliveryException> {
            adapter.send(from, "user@example.com", "Welcome", "<h1>Hello</h1>")
        }
    }
}
