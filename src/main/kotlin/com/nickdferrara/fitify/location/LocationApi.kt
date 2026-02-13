package com.nickdferrara.fitify.location

import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.Result
import java.util.UUID

data class LocationSummary(
    val id: UUID,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val timeZone: String,
    val active: Boolean,
)

interface LocationApi {
    fun findLocationById(id: UUID): Result<LocationSummary, DomainError>
    fun findAllActiveLocations(): List<LocationSummary>
}
