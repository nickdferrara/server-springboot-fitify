package com.nickdferrara.fitify.scheduling.internal.repository

import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

internal interface FitnessClassRepository :
    JpaRepository<FitnessClass, UUID>,
    JpaSpecificationExecutor<FitnessClass> {

    fun findByLocationIdAndStartTimeAfterOrderByStartTimeAsc(
        locationId: UUID,
        after: Instant,
    ): List<FitnessClass>

    @Query(
        """
        SELECT fc FROM FitnessClass fc
        WHERE fc.coachId = :coachId
          AND fc.status = 'ACTIVE'
          AND fc.startTime < :endTime
          AND fc.endTime > :startTime
        """
    )
    fun findByCoachIdAndTimeRange(coachId: UUID, startTime: Instant, endTime: Instant): List<FitnessClass>

    @Query(
        """
        SELECT DISTINCT fc FROM FitnessClass fc LEFT JOIN FETCH fc.bookings
        WHERE fc.status = 'ACTIVE' AND fc.startTime >= :start AND fc.startTime < :end
        """
    )
    fun findWithBookingsByDateRange(start: Instant, end: Instant): List<FitnessClass>
}
