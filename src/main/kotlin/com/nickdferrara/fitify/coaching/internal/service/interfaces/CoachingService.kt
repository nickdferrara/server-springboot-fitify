package com.nickdferrara.fitify.coaching.internal.service.interfaces

import com.nickdferrara.fitify.coaching.internal.dtos.request.AssignCoachLocationsRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.CreateCoachRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.UpdateCoachRequest
import com.nickdferrara.fitify.coaching.internal.dtos.response.CoachResponse
import java.util.UUID

internal interface CoachingService {
    fun findAll(): List<CoachResponse>
    fun findById(id: UUID): CoachResponse
    fun createCoach(request: CreateCoachRequest): CoachResponse
    fun updateCoach(id: UUID, request: UpdateCoachRequest): CoachResponse
    fun deactivateCoach(id: UUID)
    fun assignLocations(coachId: UUID, request: AssignCoachLocationsRequest): CoachResponse
    fun findCoachesByLocationId(locationId: UUID): List<CoachResponse>
}
