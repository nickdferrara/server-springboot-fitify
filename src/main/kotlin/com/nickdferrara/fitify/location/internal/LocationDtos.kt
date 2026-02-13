package com.nickdferrara.fitify.location.internal

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

internal data class CreateLocationRequest(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String,
    val email: String,
    val timeZone: String,
    val operatingHours: List<OperatingHoursRequest> = emptyList(),
)

internal data class UpdateLocationRequest(
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val timeZone: String? = null,
    val operatingHours: List<OperatingHoursRequest>? = null,
)

internal data class OperatingHoursRequest(
    val dayOfWeek: DayOfWeek,
    val openTime: LocalTime,
    val closeTime: LocalTime,
)

internal data class LocationResponse(
    val id: UUID,
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String,
    val email: String,
    val timeZone: String,
    val active: Boolean,
    val createdAt: Instant,
    val operatingHours: List<OperatingHoursResponse>,
)

internal data class OperatingHoursResponse(
    val id: UUID,
    val dayOfWeek: DayOfWeek,
    val openTime: LocalTime,
    val closeTime: LocalTime,
)

internal fun Location.toResponse() = LocationResponse(
    id = id!!,
    name = name,
    address = address,
    city = city,
    state = state,
    zipCode = zipCode,
    phone = phone,
    email = email,
    timeZone = timeZone,
    active = active,
    createdAt = createdAt!!,
    operatingHours = operatingHours.map { it.toResponse() },
)

internal fun LocationOperatingHours.toResponse() = OperatingHoursResponse(
    id = id!!,
    dayOfWeek = dayOfWeek,
    openTime = openTime,
    closeTime = closeTime,
)
