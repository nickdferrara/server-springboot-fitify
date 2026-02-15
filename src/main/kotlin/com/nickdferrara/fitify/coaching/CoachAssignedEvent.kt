package com.nickdferrara.fitify.coaching

import java.util.UUID

data class CoachAssignedEvent(
    val coachId: UUID,
    val classId: UUID,
)
