package com.nickdferrara.fitify.location.internal.service.interfaces

import com.nickdferrara.fitify.location.internal.dtos.request.CreateLocationRequest
import com.nickdferrara.fitify.location.internal.dtos.request.UpdateLocationRequest
import com.nickdferrara.fitify.location.internal.dtos.response.LocationResponse
import java.util.UUID

internal interface LocationService {
    fun findAll(): List<LocationResponse>
    fun findAllActive(): List<LocationResponse>
    fun findById(id: UUID): LocationResponse
    fun createLocation(request: CreateLocationRequest): LocationResponse
    fun updateLocation(id: UUID, request: UpdateLocationRequest): LocationResponse
    fun deactivateLocation(id: UUID)
}
