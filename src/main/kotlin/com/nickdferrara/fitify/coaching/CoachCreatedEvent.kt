package com.nickdferrara.fitify.coaching

import java.util.UUID

data class CoachCreatedEvent(
    val coachId: UUID,
    val name: String,
)
