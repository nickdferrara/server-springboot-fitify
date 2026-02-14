package com.nickdferrara.fitify.admin.internal.dtos.request

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

internal data class CreateRecurringScheduleRequest(
    val name: String,
    val description: String? = null,
    val classType: String,
    val coachId: UUID,
    val room: String? = null,
    val daysOfWeek: List<String>,
    val startTime: LocalTime,
    val durationMinutes: Int,
    val capacity: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
