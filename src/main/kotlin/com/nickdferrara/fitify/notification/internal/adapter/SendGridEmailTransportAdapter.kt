package com.nickdferrara.fitify.notification.internal.adapter

import com.nickdferrara.fitify.notification.internal.adapter.interfaces.EmailAddress
import com.nickdferrara.fitify.notification.internal.adapter.interfaces.EmailTransport
import com.nickdferrara.fitify.notification.internal.exception.NotificationDeliveryException
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email

internal class SendGridEmailTransportAdapter(
    private val sendGrid: SendGrid,
) : EmailTransport {

    override fun send(from: EmailAddress, to: String, subject: String, htmlBody: String) {
        try {
            val mail = Mail(
                Email(from.email, from.name),
                subject,
                Email(to),
                Content("text/html", htmlBody),
            )

            val request = Request().apply {
                method = Method.POST
                endpoint = "mail/send"
                body = mail.build()
            }

            val response = sendGrid.api(request)
            if (response.statusCode !in 200..299) {
                throw NotificationDeliveryException(
                    "SendGrid returned status ${response.statusCode} for email to $to"
                )
            }
        } catch (ex: NotificationDeliveryException) {
            throw ex
        } catch (ex: Exception) {
            throw NotificationDeliveryException("Failed to send email via SendGrid to $to", ex)
        }
    }
}
