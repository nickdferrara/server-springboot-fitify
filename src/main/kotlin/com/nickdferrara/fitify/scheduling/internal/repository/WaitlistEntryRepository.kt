package com.nickdferrara.fitify.scheduling.internal.repository

import com.nickdferrara.fitify.scheduling.internal.entities.WaitlistEntry
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface WaitlistEntryRepository : JpaRepository<WaitlistEntry, UUID> {

    fun findByFitnessClassIdOrderByPositionAsc(fitnessClassId: UUID): List<WaitlistEntry>

    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<WaitlistEntry>

    fun findByFitnessClassIdAndUserId(fitnessClassId: UUID, userId: UUID): WaitlistEntry?

    fun countByFitnessClassId(fitnessClassId: UUID): Long
}
