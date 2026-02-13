package com.nickdferrara.fitify.location.internal.dtos.request

import java.time.DayOfWeek
import java.time.LocalTime

internal data class OperatingHoursRequest(
    val dayOfWeek: DayOfWeek,
    val openTime: LocalTime,
    val closeTime: LocalTime,
)
