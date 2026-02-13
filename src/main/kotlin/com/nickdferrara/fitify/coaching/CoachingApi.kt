package com.nickdferrara.fitify.coaching

import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.Result
import java.util.UUID

data class CoachSummary(
    val id: UUID,
    val name: String,
    val active: Boolean,
)

interface CoachingApi {
    fun findCoachById(id: UUID): Result<CoachSummary, DomainError>
    fun findAllActiveCoaches(): List<CoachSummary>
    fun findActiveCoachesByLocationId(locationId: UUID): List<CoachSummary>
}
