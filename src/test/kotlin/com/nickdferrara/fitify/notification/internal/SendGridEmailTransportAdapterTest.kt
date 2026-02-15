package com.nickdferrara.fitify.notification.internal

import com.nickdferrara.fitify.notification.internal.exception.NotificationDeliveryException
import com.nickdferrara.fitify.notification.internal.adapter.SendGridEmailTransportAdapter
import com.nickdferrara.fitify.notification.internal.adapter.interfaces.EmailAddress
import com.sendgrid.Request
import com.sendgrid.Response
import com.sendgrid.SendGrid
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class SendGridEmailTransportAdapterTest {

    private val sendGrid = mockk<SendGrid>()
    private val adapter = SendGridEmailTransportAdapter(sendGrid)

    private val from = EmailAddress("noreply@fitify.com", "Fitify")

    @Test
    fun `send builds correct request and succeeds on 202`() {
        val requestSlot = slot<Request>()
        val response = Response().apply { statusCode = 202 }
        every { sendGrid.api(capture(requestSlot)) } returns response

        adapter.send(from, "user@example.com", "Welcome", "<h1>Hello</h1>")

        assertEquals("mail/send", requestSlot.captured.endpoint)
    }

    @Test
    fun `send throws NotificationDeliveryException on non-2xx status`() {
        val response = Response().apply { statusCode = 403 }
        every { sendGrid.api(any<Request>()) } returns response

        assertThrows<NotificationDeliveryException> {
            adapter.send(from, "user@example.com", "Welcome", "<h1>Hello</h1>")
        }
    }

    @Test
    fun `send wraps IOException in NotificationDeliveryException`() {
        every { sendGrid.api(any<Request>()) } throws IOException("Connection refused")

        assertThrows<NotificationDeliveryException> {
            adapter.send(from, "user@example.com", "Welcome", "<h1>Hello</h1>")
        }
    }
}
