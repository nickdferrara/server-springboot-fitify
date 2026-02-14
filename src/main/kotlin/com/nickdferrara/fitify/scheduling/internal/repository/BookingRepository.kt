package com.nickdferrara.fitify.scheduling.internal.repository

import com.nickdferrara.fitify.scheduling.internal.entities.Booking
import com.nickdferrara.fitify.scheduling.internal.entities.BookingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant
import java.util.UUID

internal interface BookingRepository : JpaRepository<Booking, UUID> {

    fun findByFitnessClassIdAndUserIdAndStatus(
        fitnessClassId: UUID,
        userId: UUID,
        status: BookingStatus,
    ): Booking?

    fun countByFitnessClassIdAndStatus(fitnessClassId: UUID, status: BookingStatus): Long

    @Query(
        """
        SELECT b FROM Booking b
        WHERE b.userId = :userId
          AND b.status = 'CONFIRMED'
          AND b.fitnessClass.startTime < :endTime
          AND b.fitnessClass.endTime > :startTime
        """
    )
    fun findOverlappingBookings(userId: UUID, startTime: Instant, endTime: Instant): List<Booking>

    @Query(
        """
        SELECT COUNT(b) FROM Booking b
        WHERE b.userId = :userId
          AND b.status = 'CONFIRMED'
          AND b.fitnessClass.startTime >= :dayStart
          AND b.fitnessClass.startTime < :dayEnd
        """
    )
    fun countUserBookingsForDay(userId: UUID, dayStart: Instant, dayEnd: Instant): Long

    fun findByFitnessClassIdAndStatus(fitnessClassId: UUID, status: BookingStatus): List<Booking>

    @Query(
        """
        SELECT COUNT(b) FROM Booking b
        WHERE b.status = 'CANCELLED' AND b.cancelledAt BETWEEN :start AND :end
        """
    )
    fun countCancellationsBetween(start: Instant, end: Instant): Long

    @Query(
        """
        SELECT COUNT(b) FROM Booking b
        WHERE b.status = 'CANCELLED' AND b.cancelledAt BETWEEN :start AND :end
          AND b.fitnessClass.locationId = :locationId
        """
    )
    fun countCancellationsBetweenAndLocationId(start: Instant, end: Instant, locationId: UUID): Long
}
