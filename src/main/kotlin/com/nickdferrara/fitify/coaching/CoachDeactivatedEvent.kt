package com.nickdferrara.fitify.coaching

import java.time.Instant
import java.util.UUID

data class CoachDeactivatedEvent(
    val coachId: UUID,
    val effectiveDate: Instant,
)
