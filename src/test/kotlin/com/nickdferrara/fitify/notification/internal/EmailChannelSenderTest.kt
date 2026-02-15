package com.nickdferrara.fitify.notification.internal

import com.nickdferrara.fitify.notification.internal.config.NotificationProperties
import com.nickdferrara.fitify.notification.internal.entities.NotificationChannel
import com.nickdferrara.fitify.notification.internal.service.EmailAddress
import com.nickdferrara.fitify.notification.internal.service.EmailChannelSender
import com.nickdferrara.fitify.notification.internal.service.EmailTransport
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class EmailChannelSenderTest {

    private val emailTransport = mockk<EmailTransport>(relaxed = true)
    private val properties = NotificationProperties(
        fromEmail = "noreply@fitify.com",
        fromName = "Fitify",
    )
    private val sender = EmailChannelSender(emailTransport, properties)

    private val userId: UUID = UUID.randomUUID()

    @Test
    fun `send delegates to EmailTransport with correct EmailAddress`() {
        val fromSlot = slot<EmailAddress>()
        val toSlot = slot<String>()
        val subjectSlot = slot<String>()
        val bodySlot = slot<String>()

        sender.send(userId, "Welcome", "<h1>Hi</h1>", mapOf("email" to "user@example.com"))

        verify {
            emailTransport.send(
                capture(fromSlot),
                capture(toSlot),
                capture(subjectSlot),
                capture(bodySlot),
            )
        }

        assertEquals("noreply@fitify.com", fromSlot.captured.email)
        assertEquals("Fitify", fromSlot.captured.name)
        assertEquals("user@example.com", toSlot.captured)
        assertEquals("Welcome", subjectSlot.captured)
        assertEquals("<h1>Hi</h1>", bodySlot.captured)
    }

    @Test
    fun `send is no-op when payload has no email`() {
        sender.send(userId, "Subject", "Body", emptyMap())

        verify(exactly = 0) { emailTransport.send(any(), any(), any(), any()) }
    }

    @Test
    fun `supports returns true for EMAIL channel`() {
        assertTrue(sender.supports(NotificationChannel.EMAIL))
    }

    @Test
    fun `supports returns false for PUSH channel`() {
        assertEquals(false, sender.supports(NotificationChannel.PUSH))
    }
}
