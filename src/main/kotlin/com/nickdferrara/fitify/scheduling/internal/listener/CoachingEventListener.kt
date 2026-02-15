package com.nickdferrara.fitify.scheduling.internal.listener

import com.nickdferrara.fitify.coaching.CoachAssignedEvent
import com.nickdferrara.fitify.coaching.CoachUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
internal class CoachingEventListener {

    private val logger = LoggerFactory.getLogger(CoachingEventListener::class.java)

    @TransactionalEventListener
    fun onCoachAssigned(event: CoachAssignedEvent) {
        logger.info("Coach {} assigned to class {}", event.coachId, event.classId)
    }

    @TransactionalEventListener
    fun onCoachUpdated(event: CoachUpdatedEvent) {
        logger.info("Coach {} updated fields: {}", event.coachId, event.updatedFields)
    }
}
