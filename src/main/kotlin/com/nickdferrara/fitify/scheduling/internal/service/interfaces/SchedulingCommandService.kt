package com.nickdferrara.fitify.scheduling.internal.service.interfaces

import com.nickdferrara.fitify.scheduling.internal.dtos.request.CreateClassRequest
import com.nickdferrara.fitify.scheduling.internal.dtos.request.UpdateClassRequest
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ClassResponse
import com.nickdferrara.fitify.scheduling.internal.dtos.response.WaitlistEntryResponse
import com.nickdferrara.fitify.scheduling.internal.model.BookClassResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

internal interface SchedulingCommandService {
    var cancellationWindowHours: Long
    var maxWaitlistSize: Int
    var maxBookingsPerDay: Int

    fun searchClasses(
        date: LocalDate?,
        classType: String?,
        coachId: UUID?,
        locationId: UUID?,
        available: Boolean?,
        pageable: Pageable,
    ): Page<ClassResponse>

    fun createClass(locationId: UUID, request: CreateClassRequest): ClassResponse
    fun getClass(classId: UUID): ClassResponse
    fun updateClass(classId: UUID, request: UpdateClassRequest): ClassResponse
    fun cancelClassInternal(classId: UUID)
    fun bookClass(classId: UUID, userId: UUID): BookClassResult
    fun cancelBooking(classId: UUID, userId: UUID)
    fun getUserWaitlistEntries(userId: UUID): List<WaitlistEntryResponse>
    fun removeFromWaitlist(classId: UUID, userId: UUID)
}
