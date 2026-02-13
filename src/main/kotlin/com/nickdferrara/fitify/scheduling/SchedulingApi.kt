package com.nickdferrara.fitify.scheduling

import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.Result
import java.time.Instant
import java.util.UUID

data class ClassSummary(
    val id: UUID,
    val name: String,
    val classType: String,
    val startTime: Instant,
    val endTime: Instant,
    val locationId: UUID,
    val coachId: UUID,
)

interface SchedulingApi {
    fun findClassById(id: UUID): Result<ClassSummary, DomainError>
    fun findUpcomingClassesByLocationId(locationId: UUID): List<ClassSummary>
}
