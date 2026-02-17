package com.nickdferrara.fitify.coaching.internal

import com.nickdferrara.fitify.coaching.CoachCreatedEvent
import com.nickdferrara.fitify.coaching.CoachDeactivatedEvent
import com.nickdferrara.fitify.coaching.CoachUpdatedEvent
import com.nickdferrara.fitify.coaching.internal.dtos.request.AssignCoachLocationsRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.CertificationRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.CreateCoachRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.UpdateCoachRequest
import com.nickdferrara.fitify.coaching.internal.entities.Coach
import com.nickdferrara.fitify.coaching.internal.entities.CoachCertification
import com.nickdferrara.fitify.coaching.internal.entities.CoachLocation
import com.nickdferrara.fitify.coaching.internal.repository.CoachRepository
import com.nickdferrara.fitify.coaching.internal.service.CoachNotFoundException
import com.nickdferrara.fitify.coaching.internal.service.CoachingServiceImpl
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
import java.time.Instant
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

class CoachingServiceTest {

    private val coachRepository = mockk<CoachRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val coachingService = CoachingServiceImpl(coachRepository, eventPublisher)

    private fun buildCoach(
        id: UUID = UUID.randomUUID(),
        name: String = "Jane Smith",
        bio: String = "Experienced fitness coach",
        active: Boolean = true,
    ) = Coach(
        id = id,
        name = name,
        bio = bio,
        photoUrl = "https://example.com/jane.jpg",
        specializations = listOf("yoga", "pilates"),
        active = active,
        createdAt = Instant.now(),
    )

    // --- Public API Tests ---

    @Test
    fun `findCoachById returns summary when coach exists`() {
        val id = UUID.randomUUID()
        val coach = buildCoach(id = id)
        every { coachRepository.findById(id) } returns Optional.of(coach)

        val result = coachingService.findCoachById(id)

        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).value
        assertEquals(id, summary.id)
        assertEquals("Jane Smith", summary.name)
        assertTrue(summary.active)
    }

    @Test
    fun `findCoachById returns failure when coach does not exist`() {
        val id = UUID.randomUUID()
        every { coachRepository.findById(id) } returns Optional.empty()

        val result = coachingService.findCoachById(id)

        assertTrue(result is Result.Failure)
        val error = (result as Result.Failure).error
        assertTrue(error is NotFoundError)
    }

    @Test
    fun `findAllActiveCoaches returns only active coaches`() {
        val active = buildCoach(name = "Active Coach", active = true)
        every { coachRepository.findByActiveTrue() } returns listOf(active)

        val results = coachingService.findAllActiveCoaches()

        assertEquals(1, results.size)
        assertEquals("Active Coach", results[0].name)
    }

    @Test
    fun `findActiveCoachesByLocationId delegates to repository`() {
        val locationId = UUID.randomUUID()
        val coach = buildCoach(name = "Location Coach")
        every { coachRepository.findActiveCoachesByLocationId(locationId) } returns listOf(coach)

        val results = coachingService.findActiveCoachesByLocationId(locationId)

        assertEquals(1, results.size)
        assertEquals("Location Coach", results[0].name)
    }

    // --- Create Tests ---

    @Test
    fun `createCoach persists and publishes event`() {
        val request = CreateCoachRequest(
            name = "John Doe",
            bio = "Strength training expert",
            photoUrl = "https://example.com/john.jpg",
            specializations = listOf("weightlifting", "crossfit"),
            certifications = listOf(
                CertificationRequest(
                    name = "NASM CPT",
                    issuer = "NASM",
                    validUntil = LocalDate.of(2027, 12, 31),
                )
            ),
        )

        val savedId = UUID.randomUUID()
        every { coachRepository.save(any()) } answers {
            val coach = firstArg<Coach>()
            Coach(
                id = savedId,
                name = coach.name,
                bio = coach.bio,
                photoUrl = coach.photoUrl,
                specializations = coach.specializations,
                active = coach.active,
                createdAt = Instant.now(),
            ).also { saved ->
                coach.certifications.forEach { cert ->
                    saved.certifications.add(
                        CoachCertification(
                            id = UUID.randomUUID(),
                            coach = saved,
                            name = cert.name,
                            issuer = cert.issuer,
                            validUntil = cert.validUntil,
                        )
                    )
                }
            }
        }

        val response = coachingService.createCoach(request)

        assertEquals(savedId, response.id)
        assertEquals("John Doe", response.name)
        assertEquals("Strength training expert", response.bio)
        assertEquals(listOf("weightlifting", "crossfit"), response.specializations)
        assertEquals(1, response.certifications.size)
        assertEquals("NASM CPT", response.certifications[0].name)

        val eventSlot = slot<CoachCreatedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(savedId, eventSlot.captured.coachId)
        assertEquals("John Doe", eventSlot.captured.name)
    }

    // --- Update Tests ---

    @Test
    fun `updateCoach updates fields and publishes event`() {
        val id = UUID.randomUUID()
        val existing = buildCoach(id = id, name = "Old Name")
        every { coachRepository.findById(id) } returns Optional.of(existing)
        every { coachRepository.save(any()) } answers { firstArg() }

        val request = UpdateCoachRequest(name = "New Name", bio = "Updated bio")

        val response = coachingService.updateCoach(id, request)

        assertEquals("New Name", response.name)
        assertEquals("Updated bio", response.bio)
        assertEquals("https://example.com/jane.jpg", response.photoUrl) // unchanged

        val eventSlot = slot<CoachUpdatedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(setOf("name", "bio"), eventSlot.captured.updatedFields)
    }

    @Test
    fun `updateCoach with no changes does not publish event`() {
        val id = UUID.randomUUID()
        val existing = buildCoach(id = id)
        every { coachRepository.findById(id) } returns Optional.of(existing)
        every { coachRepository.save(any()) } answers { firstArg() }

        val request = UpdateCoachRequest() // all null

        coachingService.updateCoach(id, request)

        verify(exactly = 0) { eventPublisher.publishEvent(any<CoachUpdatedEvent>()) }
    }

    @Test
    fun `updateCoach throws when coach not found`() {
        val id = UUID.randomUUID()
        every { coachRepository.findById(id) } returns Optional.empty()

        assertThrows<CoachNotFoundException> {
            coachingService.updateCoach(id, UpdateCoachRequest(name = "X"))
        }
    }

    @Test
    fun `updateCoach replaces certifications when provided`() {
        val id = UUID.randomUUID()
        val existing = buildCoach(id = id)
        existing.certifications.add(
            CoachCertification(
                id = UUID.randomUUID(),
                coach = existing,
                name = "Old Cert",
                issuer = "Old Issuer",
            )
        )
        every { coachRepository.findById(id) } returns Optional.of(existing)
        every { coachRepository.save(any()) } answers {
            val coach = firstArg<Coach>()
            coach.certifications.forEachIndexed { index, cert ->
                if (cert.id == null) {
                    coach.certifications[index] = CoachCertification(
                        id = UUID.randomUUID(),
                        coach = cert.coach,
                        name = cert.name,
                        issuer = cert.issuer,
                        validUntil = cert.validUntil,
                    )
                }
            }
            coach
        }

        val request = UpdateCoachRequest(
            certifications = listOf(
                CertificationRequest(name = "New Cert", issuer = "New Issuer", validUntil = LocalDate.of(2028, 6, 15)),
            )
        )

        val response = coachingService.updateCoach(id, request)

        assertEquals(1, response.certifications.size)
        assertEquals("New Cert", response.certifications[0].name)
        assertEquals("New Issuer", response.certifications[0].issuer)
    }

    // --- Deactivate Tests ---

    @Test
    fun `deactivateCoach sets active false and publishes event`() {
        val id = UUID.randomUUID()
        val existing = buildCoach(id = id, active = true)
        every { coachRepository.findById(id) } returns Optional.of(existing)
        every { coachRepository.save(any()) } answers { firstArg() }

        coachingService.deactivateCoach(id)

        assertFalse(existing.active)

        val eventSlot = slot<CoachDeactivatedEvent>()
        verify { eventPublisher.publishEvent(capture(eventSlot)) }
        assertEquals(id, eventSlot.captured.coachId)
        assertNotNull(eventSlot.captured.effectiveDate)
    }

    @Test
    fun `deactivateCoach throws when coach not found`() {
        val id = UUID.randomUUID()
        every { coachRepository.findById(id) } returns Optional.empty()

        assertThrows<CoachNotFoundException> {
            coachingService.deactivateCoach(id)
        }
    }

    // --- Location Assignment Tests ---

    @Test
    fun `assignLocations replaces coach locations`() {
        val coachId = UUID.randomUUID()
        val existing = buildCoach(id = coachId)
        val oldLocationId = UUID.randomUUID()
        existing.locations.add(
            CoachLocation(
                id = UUID.randomUUID(),
                coach = existing,
                locationId = oldLocationId,
                assignedAt = Instant.now(),
            )
        )

        every { coachRepository.findById(coachId) } returns Optional.of(existing)
        every { coachRepository.save(any()) } answers {
            val coach = firstArg<Coach>()
            coach.locations.forEachIndexed { index, loc ->
                if (loc.id == null) {
                    coach.locations[index] = CoachLocation(
                        id = UUID.randomUUID(),
                        coach = loc.coach,
                        locationId = loc.locationId,
                        assignedAt = Instant.now(),
                    )
                }
            }
            coach
        }

        val newLoc1 = UUID.randomUUID()
        val newLoc2 = UUID.randomUUID()
        val request = AssignCoachLocationsRequest(locationIds = listOf(newLoc1, newLoc2))

        val response = coachingService.assignLocations(coachId, request)

        assertEquals(2, response.locationIds.size)
        assertTrue(response.locationIds.contains(newLoc1))
        assertTrue(response.locationIds.contains(newLoc2))
        assertFalse(response.locationIds.contains(oldLocationId))
    }

    @Test
    fun `assignLocations throws when coach not found`() {
        val coachId = UUID.randomUUID()
        every { coachRepository.findById(coachId) } returns Optional.empty()

        assertThrows<CoachNotFoundException> {
            coachingService.assignLocations(coachId, AssignCoachLocationsRequest(listOf(UUID.randomUUID())))
        }
    }

    // --- Internal Method Tests ---

    @Test
    fun `findById throws when coach not found`() {
        val id = UUID.randomUUID()
        every { coachRepository.findById(id) } returns Optional.empty()

        assertThrows<CoachNotFoundException> {
            coachingService.findById(id)
        }
    }
}
