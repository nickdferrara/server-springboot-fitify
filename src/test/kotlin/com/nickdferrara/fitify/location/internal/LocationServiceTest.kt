package com.nickdferrara.fitify.location.internal

import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.location.LocationDeactivatedEvent
import com.nickdferrara.fitify.location.LocationUpdatedEvent
import com.nickdferrara.fitify.location.internal.dtos.request.CreateLocationRequest
import com.nickdferrara.fitify.location.internal.dtos.request.OperatingHoursRequest
import com.nickdferrara.fitify.location.internal.dtos.request.UpdateLocationRequest
import com.nickdferrara.fitify.location.internal.entities.Location
import com.nickdferrara.fitify.location.internal.entities.LocationOperatingHours
import com.nickdferrara.fitify.location.internal.repository.LocationRepository
import com.nickdferrara.fitify.location.internal.service.LocationNotFoundException
import com.nickdferrara.fitify.location.internal.service.LocationServiceImpl
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.util.Optional
import java.util.UUID

class LocationServiceTest {

    private val locationRepository = mockk<LocationRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val locationService = LocationServiceImpl(locationRepository, eventPublisher)

    private fun buildLocation(
        id: UUID = UUID.randomUUID(),
        name: String = "Downtown Gym",
        active: Boolean = true,
    ) = Location(
        id = id,
        name = name,
        address = "123 Main St",
        city = "New York",
        state = "NY",
        zipCode = "10001",
        phone = "555-0100",
        email = "downtown@fitify.com",
        timeZone = "America/New_York",
        active = active,
        createdAt = Instant.now(),
    )

    @Test
    fun `findLocationById returns summary when location exists`() {
        val id = UUID.randomUUID()
        val location = buildLocation(id = id)
        every { locationRepository.findById(id) } returns Optional.of(location)

        val result = locationService.findLocationById(id)

        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).value
        assertEquals(id, summary.id)
        assertEquals("Downtown Gym", summary.name)
        assertEquals("America/New_York", summary.timeZone)
    }

    @Test
    fun `findLocationById returns failure when location does not exist`() {
        val id = UUID.randomUUID()
        every { locationRepository.findById(id) } returns Optional.empty()

        val result = locationService.findLocationById(id)

        assertTrue(result is Result.Failure)
        val error = (result as Result.Failure).error
        assertTrue(error is NotFoundError)
    }

    @Test
    fun `findAllActiveLocations returns only active locations`() {
        val active = buildLocation(name = "Active Gym", active = true)
        every { locationRepository.findByActiveTrue() } returns listOf(active)

        val results = locationService.findAllActiveLocations()

        assertEquals(1, results.size)
        assertEquals("Active Gym", results[0].name)
    }

    @Test
    fun `createLocation persists and publishes event`() {
        val request = CreateLocationRequest(
            name = "Uptown Gym",
            address = "456 Elm St",
            city = "New York",
            state = "NY",
            zipCode = "10002",
            phone = "555-0200",
            email = "uptown@fitify.com",
            timeZone = "America/New_York",
            operatingHours = listOf(
                OperatingHoursRequest(
                    dayOfWeek = DayOfWeek.MONDAY,
                    openTime = LocalTime.of(6, 0),
                    closeTime = LocalTime.of(22, 0),
                )
            ),
        )

        val savedId = UUID.randomUUID()
        every { locationRepository.save(any()) } answers {
            val loc = firstArg<Location>()
            Location(
                id = savedId,
                name = loc.name,
                address = loc.address,
                city = loc.city,
                state = loc.state,
                zipCode = loc.zipCode,
                phone = loc.phone,
                email = loc.email,
                timeZone = loc.timeZone,
                active = loc.active,
                createdAt = Instant.now(),
            ).also { saved ->
                loc.operatingHours.forEach { hours ->
                    saved.operatingHours.add(
                        LocationOperatingHours(
                            id = UUID.randomUUID(),
                            location = saved,
                            dayOfWeek = hours.dayOfWeek,
                            openTime = hours.openTime,
                            closeTime = hours.closeTime,
                        )
                    )
                }
            }
        }

        val response = locationService.createLocation(request)

        assertEquals(savedId, response.id)
        assertEquals("Uptown Gym", response.name)
        assertEquals(1, response.operatingHours.size)
        assertEquals(DayOfWeek.MONDAY, response.operatingHours[0].dayOfWeek)

        val eventSlot = slot<LocationCreatedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(savedId, eventSlot.captured.locationId)
        assertEquals("Uptown Gym", eventSlot.captured.name)
    }

    @Test
    fun `updateLocation updates fields and publishes event`() {
        val id = UUID.randomUUID()
        val existing = buildLocation(id = id, name = "Old Name")
        every { locationRepository.findById(id) } returns Optional.of(existing)
        every { locationRepository.save(any()) } answers { firstArg() }

        val request = UpdateLocationRequest(name = "New Name", phone = "555-9999")

        val response = locationService.updateLocation(id, request)

        assertEquals("New Name", response.name)
        assertEquals("555-9999", response.phone)
        assertEquals("123 Main St", response.address) // unchanged

        val eventSlot = slot<LocationUpdatedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(setOf("name", "phone"), eventSlot.captured.updatedFields)
    }

    @Test
    fun `updateLocation with no changes does not publish event`() {
        val id = UUID.randomUUID()
        val existing = buildLocation(id = id)
        every { locationRepository.findById(id) } returns Optional.of(existing)
        every { locationRepository.save(any()) } answers { firstArg() }

        val request = UpdateLocationRequest() // all null

        locationService.updateLocation(id, request)

        verify(exactly = 0) { eventPublisher.publishEvent(any<LocationUpdatedEvent>()) }
    }

    @Test
    fun `updateLocation throws when location not found`() {
        val id = UUID.randomUUID()
        every { locationRepository.findById(id) } returns Optional.empty()

        assertThrows<LocationNotFoundException> {
            locationService.updateLocation(id, UpdateLocationRequest(name = "X"))
        }
    }

    @Test
    fun `deactivateLocation sets active false and publishes event`() {
        val id = UUID.randomUUID()
        val existing = buildLocation(id = id, active = true)
        every { locationRepository.findById(id) } returns Optional.of(existing)
        every { locationRepository.save(any()) } answers { firstArg() }

        locationService.deactivateLocation(id)

        assertFalse(existing.active)

        val eventSlot = slot<LocationDeactivatedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(id, eventSlot.captured.locationId)
        assertNotNull(eventSlot.captured.effectiveDate)
    }

    @Test
    fun `deactivateLocation throws when location not found`() {
        val id = UUID.randomUUID()
        every { locationRepository.findById(id) } returns Optional.empty()

        assertThrows<LocationNotFoundException> {
            locationService.deactivateLocation(id)
        }
    }

    @Test
    fun `findById throws when location not found`() {
        val id = UUID.randomUUID()
        every { locationRepository.findById(id) } returns Optional.empty()

        assertThrows<LocationNotFoundException> {
            locationService.findById(id)
        }
    }

    @Test
    fun `updateLocation replaces operating hours when provided`() {
        val id = UUID.randomUUID()
        val existing = buildLocation(id = id)
        existing.operatingHours.add(
            LocationOperatingHours(
                id = UUID.randomUUID(),
                location = existing,
                dayOfWeek = DayOfWeek.MONDAY,
                openTime = LocalTime.of(6, 0),
                closeTime = LocalTime.of(22, 0),
            )
        )
        every { locationRepository.findById(id) } returns Optional.of(existing)
        every { locationRepository.save(any()) } answers {
            val loc = firstArg<Location>()
            // Simulate JPA assigning IDs to new operating hours
            loc.operatingHours.forEachIndexed { index, hours ->
                if (hours.id == null) {
                    loc.operatingHours[index] = LocationOperatingHours(
                        id = UUID.randomUUID(),
                        location = hours.location,
                        dayOfWeek = hours.dayOfWeek,
                        openTime = hours.openTime,
                        closeTime = hours.closeTime,
                    )
                }
            }
            loc
        }

        val request = UpdateLocationRequest(
            operatingHours = listOf(
                OperatingHoursRequest(
                    dayOfWeek = DayOfWeek.TUESDAY,
                    openTime = LocalTime.of(7, 0),
                    closeTime = LocalTime.of(21, 0),
                ),
                OperatingHoursRequest(
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    openTime = LocalTime.of(7, 0),
                    closeTime = LocalTime.of(21, 0),
                ),
            )
        )

        val response = locationService.updateLocation(id, request)

        assertEquals(2, response.operatingHours.size)
        assertEquals(DayOfWeek.TUESDAY, response.operatingHours[0].dayOfWeek)
        assertEquals(DayOfWeek.WEDNESDAY, response.operatingHours[1].dayOfWeek)
    }
}
