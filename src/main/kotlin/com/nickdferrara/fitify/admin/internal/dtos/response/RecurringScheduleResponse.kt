package com.nickdferrara.fitify.admin.internal.dtos.response

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

internal data class RecurringScheduleResponse(
    val id: UUID,
    val locationId: UUID,
    val name: String,
    val description: String?,
    val classType: String,
    val coachId: UUID,
    val room: String?,
    val daysOfWeek: List<String>,
    val startTime: LocalTime,
    val durationMinutes: Int,
    val capacity: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val classesCreated: Int,
)
