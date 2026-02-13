package com.nickdferrara.fitify.scheduling

import java.time.Instant
import java.util.UUID

data class ClassBookedEvent(
    val userId: UUID,
    val classId: UUID,
    val className: String,
    val startTime: Instant,
)
