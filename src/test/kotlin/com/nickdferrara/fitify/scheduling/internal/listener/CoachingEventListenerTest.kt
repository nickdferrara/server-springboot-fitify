package com.nickdferrara.fitify.scheduling.internal.listener

import com.nickdferrara.fitify.coaching.CoachAssignedEvent
import com.nickdferrara.fitify.coaching.CoachUpdatedEvent
import org.junit.jupiter.api.Test
import java.util.UUID

class CoachingEventListenerTest {

    private val listener = CoachingEventListener()

    @Test
    fun `onCoachAssigned handles event without error`() {
        val event = CoachAssignedEvent(coachId = UUID.randomUUID(), classId = UUID.randomUUID())

        listener.onCoachAssigned(event)
    }

    @Test
    fun `onCoachUpdated handles event without error`() {
        val event = CoachUpdatedEvent(coachId = UUID.randomUUID(), updatedFields = setOf("name", "bio"))

        listener.onCoachUpdated(event)
    }
}
