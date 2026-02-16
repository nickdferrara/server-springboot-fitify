package com.nickdferrara.fitify.location.internal.dtos.response

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

internal data class OperatingHoursResponse(
    val id: UUID,
    val dayOfWeek: DayOfWeek,
    val openTime: LocalTime,
    val closeTime: LocalTime,
)
