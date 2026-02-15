package com.nickdferrara.fitify.notification.internal.config

import com.nickdferrara.fitify.notification.internal.service.EmailTransport
import com.nickdferrara.fitify.notification.internal.service.SendGridEmailTransportAdapter
import com.nickdferrara.fitify.notification.internal.service.SmtpEmailTransportAdapter
import com.sendgrid.SendGrid
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

@Configuration
internal class EmailTransportConfig {

    @Bean
    @ConditionalOnProperty(
        name = ["fitify.notification.email.provider"],
        havingValue = "smtp",
        matchIfMissing = true,
    )
    fun smtpEmailTransport(mailSender: JavaMailSender): EmailTransport =
        SmtpEmailTransportAdapter(mailSender)

    @Bean
    @ConditionalOnProperty(
        name = ["fitify.notification.email.provider"],
        havingValue = "sendgrid",
    )
    fun sendGridEmailTransport(
        @Value("\${fitify.notification.sendgrid.api-key}") apiKey: String,
    ): EmailTransport =
        SendGridEmailTransportAdapter(SendGrid(apiKey))
}
