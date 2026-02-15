package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.time.Instant
import java.util.UUID

internal class ScheduleConflictException(userId: UUID, startTime: Instant, endTime: Instant) :
    RuntimeException("User $userId has an overlapping booking between $startTime and $endTime")
