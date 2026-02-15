package com.nickdferrara.fitify.admin.internal.dtos.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

internal data class CreateRecurringScheduleRequest(
    @field:NotBlank
    val name: String,
    val description: String? = null,
    @field:NotBlank
    val classType: String,
    val coachId: UUID,
    val room: String? = null,
    @field:NotEmpty
    val daysOfWeek: List<String>,
    val startTime: LocalTime,
    @field:Positive
    val durationMinutes: Int,
    @field:Positive
    val capacity: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
