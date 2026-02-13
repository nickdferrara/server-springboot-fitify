package com.nickdferrara.fitify.coaching.internal.repository

import com.nickdferrara.fitify.coaching.internal.entities.Coach
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

internal interface CoachRepository : JpaRepository<Coach, UUID> {

    fun findByActiveTrue(): List<Coach>

    @Query("SELECT c FROM Coach c JOIN c.locations cl WHERE cl.locationId = :locationId AND c.active = true")
    fun findActiveCoachesByLocationId(locationId: UUID): List<Coach>

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Coach c JOIN c.locations cl WHERE c.id = :coachId AND cl.locationId = :locationId AND c.active = true")
    fun isCoachActiveAndAssignedToLocation(coachId: UUID, locationId: UUID): Boolean
}
