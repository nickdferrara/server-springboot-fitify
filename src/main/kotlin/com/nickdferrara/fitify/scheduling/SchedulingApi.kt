package com.nickdferrara.fitify.scheduling

import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.Result
import java.time.Instant
import java.util.UUID

data class ClassSummary(
    val id: UUID,
    val name: String,
    val description: String?,
    val classType: String,
    val startTime: Instant,
    val endTime: Instant,
    val locationId: UUID,
    val coachId: UUID,
)

data class ClassDetail(
    val id: UUID,
    val name: String,
    val description: String?,
    val classType: String,
    val coachId: UUID,
    val room: String?,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int,
    val locationId: UUID,
    val status: String,
    val enrolledCount: Int,
    val waitlistSize: Int,
    val createdAt: Instant,
)

data class CreateClassCommand(
    val name: String,
    val description: String? = null,
    val classType: String,
    val coachId: UUID,
    val room: String? = null,
    val startTime: Instant,
    val endTime: Instant,
    val capacity: Int,
)

data class UpdateClassCommand(
    val name: String? = null,
    val description: String? = null,
    val classType: String? = null,
    val coachId: UUID? = null,
    val room: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val capacity: Int? = null,
)

data class CancelClassResult(
    val classId: UUID,
    val className: String,
    val affectedUserIds: List<UUID>,
    val waitlistUserIds: List<UUID>,
)

data class ClassUtilizationSummary(
    val classId: UUID,
    val locationId: UUID,
    val classType: String,
    val capacity: Int,
    val enrolledCount: Int,
)

interface SchedulingApi {
    fun findClassById(id: UUID): Result<ClassSummary, DomainError>
    fun findUpcomingClassesByLocationId(locationId: UUID): List<ClassSummary>
    fun createClass(locationId: UUID, command: CreateClassCommand): ClassDetail
    fun updateClass(classId: UUID, command: UpdateClassCommand): ClassDetail
    fun cancelClass(classId: UUID): CancelClassResult
    fun getClassDetail(classId: UUID): Result<ClassDetail, DomainError>
    fun findClassesByLocationId(locationId: UUID): List<ClassDetail>
    fun findClassesByCoachIdAndTimeRange(coachId: UUID, startTime: Instant, endTime: Instant): List<ClassSummary>
    fun getClassUtilizationByDateRange(start: Instant, end: Instant): List<ClassUtilizationSummary>
    fun countBookingCancellationsBetween(start: Instant, end: Instant, locationId: UUID?): Long
}
