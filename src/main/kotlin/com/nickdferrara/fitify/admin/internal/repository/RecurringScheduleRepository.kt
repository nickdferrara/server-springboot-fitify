package com.nickdferrara.fitify.admin.internal.repository

import com.nickdferrara.fitify.admin.internal.entities.RecurringSchedule
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface RecurringScheduleRepository : JpaRepository<RecurringSchedule, UUID> {

    fun findByLocationIdAndActiveTrue(locationId: UUID): List<RecurringSchedule>
}
