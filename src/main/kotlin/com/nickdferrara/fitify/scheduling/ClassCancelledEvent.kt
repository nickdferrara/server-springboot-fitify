package com.nickdferrara.fitify.scheduling

import java.time.Instant
import java.util.UUID

data class ClassCancelledEvent(
    val classId: UUID,
    val className: String,
    val locationId: UUID,
    val originalStartTime: Instant,
    val affectedUserIds: List<UUID>,
    val waitlistUserIds: List<UUID>,
    val cancelledAt: Instant,
)
