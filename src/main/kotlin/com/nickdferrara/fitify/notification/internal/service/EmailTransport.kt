package com.nickdferrara.fitify.notification.internal.service

internal interface EmailTransport {
    fun send(from: EmailAddress, to: String, subject: String, htmlBody: String)
}

internal data class EmailAddress(val email: String, val name: String)
