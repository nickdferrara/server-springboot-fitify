package com.nickdferrara.fitify.scheduling

import java.time.Instant
import java.util.UUID

data class BookingCancelledEvent(
    val userId: UUID,
    val classId: UUID,
    val cancelledAt: Instant,
)
