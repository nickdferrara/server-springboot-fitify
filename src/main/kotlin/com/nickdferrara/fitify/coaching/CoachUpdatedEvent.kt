package com.nickdferrara.fitify.coaching

import java.util.UUID

data class CoachUpdatedEvent(
    val coachId: UUID,
    val updatedFields: Set<String>,
)
