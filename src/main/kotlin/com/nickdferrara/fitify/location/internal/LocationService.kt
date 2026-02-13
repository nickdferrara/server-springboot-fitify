package com.nickdferrara.fitify.location.internal

import com.nickdferrara.fitify.location.LocationApi
import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationSummary
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
internal class LocationService(
    private val locationRepository: LocationRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : LocationApi {

    override fun findLocationById(id: UUID): Result<LocationSummary, DomainError> {
        val location = locationRepository.findById(id).orElse(null)
            ?: return Result.Failure(NotFoundError("Location not found: $id"))
        return Result.Success(location.toSummary())
    }

    override fun findAllActiveLocations(): List<LocationSummary> {
        return locationRepository.findByActiveTrue().map { it.toSummary() }
    }

    fun findAll(): List<LocationResponse> {
        return locationRepository.findAll().map { it.toResponse() }
    }

    fun findAllActive(): List<LocationResponse> {
        return locationRepository.findByActiveTrue().map { it.toResponse() }
    }

    fun findById(id: UUID): LocationResponse {
        val location = locationRepository.findById(id)
            .orElseThrow { LocationNotFoundException(id) }
        return location.toResponse()
    }

    @Transactional
    fun createLocation(request: CreateLocationRequest): LocationResponse {
        val location = Location(
            name = request.name,
            address = request.address,
            city = request.city,
            state = request.state,
            zipCode = request.zipCode,
            phone = request.phone,
            email = request.email,
            timeZone = request.timeZone,
        )

        request.operatingHours.forEach { hours ->
            location.operatingHours.add(
                LocationOperatingHours(
                    location = location,
                    dayOfWeek = hours.dayOfWeek,
                    openTime = hours.openTime,
                    closeTime = hours.closeTime,
                )
            )
        }

        val saved = locationRepository.save(location)

        eventPublisher.publishEvent(
            LocationCreatedEvent(
                locationId = saved.id!!,
                name = saved.name,
                address = saved.address,
                timeZone = saved.timeZone,
            )
        )

        return saved.toResponse()
    }

    @Transactional
    fun updateLocation(id: UUID, request: UpdateLocationRequest): LocationResponse {
        val location = locationRepository.findById(id)
            .orElseThrow { LocationNotFoundException(id) }

        val updatedFields = mutableSetOf<String>()

        request.name?.let { location.name = it; updatedFields.add("name") }
        request.address?.let { location.address = it; updatedFields.add("address") }
        request.city?.let { location.city = it; updatedFields.add("city") }
        request.state?.let { location.state = it; updatedFields.add("state") }
        request.zipCode?.let { location.zipCode = it; updatedFields.add("zipCode") }
        request.phone?.let { location.phone = it; updatedFields.add("phone") }
        request.email?.let { location.email = it; updatedFields.add("email") }
        request.timeZone?.let { location.timeZone = it; updatedFields.add("timeZone") }

        request.operatingHours?.let { hoursList ->
            location.operatingHours.clear()
            hoursList.forEach { hours ->
                location.operatingHours.add(
                    LocationOperatingHours(
                        location = location,
                        dayOfWeek = hours.dayOfWeek,
                        openTime = hours.openTime,
                        closeTime = hours.closeTime,
                    )
                )
            }
            updatedFields.add("operatingHours")
        }

        val saved = locationRepository.save(location)

        if (updatedFields.isNotEmpty()) {
            eventPublisher.publishEvent(
                LocationUpdatedEvent(
                    locationId = saved.id!!,
                    updatedFields = updatedFields,
                )
            )
        }

        return saved.toResponse()
    }

    @Transactional
    fun deactivateLocation(id: UUID) {
        val location = locationRepository.findById(id)
            .orElseThrow { LocationNotFoundException(id) }

        location.active = false
        locationRepository.save(location)

        eventPublisher.publishEvent(
            LocationDeactivatedEvent(
                locationId = location.id!!,
                effectiveDate = Instant.now(),
            )
        )
    }

    private fun Location.toSummary() = LocationSummary(
        id = id!!,
        name = name,
        address = address,
        city = city,
        state = state,
        zipCode = zipCode,
        timeZone = timeZone,
        active = active,
    )
}

internal class LocationNotFoundException(id: UUID) :
    RuntimeException("Location not found: $id")
