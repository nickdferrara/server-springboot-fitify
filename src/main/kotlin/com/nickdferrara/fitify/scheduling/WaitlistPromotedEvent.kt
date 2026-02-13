package com.nickdferrara.fitify.scheduling

import java.time.Instant
import java.util.UUID

data class WaitlistPromotedEvent(
    val userId: UUID,
    val classId: UUID,
    val className: String,
    val startTime: Instant,
)
