package com.nickdferrara.fitify.security.internal.service

import com.nickdferrara.fitify.security.internal.entities.LocationAdminAssignment
import com.nickdferrara.fitify.security.internal.exception.AssignmentAlreadyExistsException
import com.nickdferrara.fitify.security.internal.exception.AssignmentNotFoundException
import com.nickdferrara.fitify.security.internal.repository.LocationAdminAssignmentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID

class LocationAdminServiceTest {

    private val repository = mockk<LocationAdminAssignmentRepository>()
    private val service = LocationAdminService(repository)

    private val locationId = UUID.randomUUID()
    private val keycloakId = UUID.randomUUID().toString()

    private fun mockAuthentication(roles: List<String>, subject: String = keycloakId): Authentication {
        val jwt = mockk<Jwt>()
        every { jwt.subject } returns subject

        val auth = mockk<Authentication>()
        every { auth.authorities } returns roles.map { SimpleGrantedAuthority(it) }
        every { auth.principal } returns jwt
        return auth
    }

    @Test
    fun `hasAccess returns true for ROLE_ADMIN regardless of assignment`() {
        val auth = mockAuthentication(listOf("ROLE_ADMIN"))

        assertTrue(service.hasAccess(auth, locationId))
    }

    @Test
    fun `hasAccess returns true for LOCATION_ADMIN with matching assignment`() {
        val auth = mockAuthentication(listOf("ROLE_LOCATION_ADMIN"))
        every { repository.existsByKeycloakIdAndLocationId(keycloakId, locationId) } returns true

        assertTrue(service.hasAccess(auth, locationId))
    }

    @Test
    fun `hasAccess returns false for LOCATION_ADMIN without matching assignment`() {
        val auth = mockAuthentication(listOf("ROLE_LOCATION_ADMIN"))
        every { repository.existsByKeycloakIdAndLocationId(keycloakId, locationId) } returns false

        assertFalse(service.hasAccess(auth, locationId))
    }

    @Test
    fun `hasAccess returns false for ROLE_USER`() {
        val auth = mockAuthentication(listOf("ROLE_USER"))

        assertFalse(service.hasAccess(auth, locationId))
    }

    @Test
    fun `assignLocationAdmin creates new assignment`() {
        every { repository.existsByKeycloakIdAndLocationId(keycloakId, locationId) } returns false
        every { repository.save(any()) } answers {
            val arg = firstArg<LocationAdminAssignment>()
            LocationAdminAssignment(
                id = UUID.randomUUID(),
                keycloakId = arg.keycloakId,
                locationId = arg.locationId,
                createdAt = Instant.now(),
            )
        }

        val response = service.assignLocationAdmin(keycloakId, locationId)

        assertEquals(keycloakId, response.keycloakId)
        assertEquals(locationId, response.locationId)
    }

    @Test
    fun `assignLocationAdmin throws when assignment already exists`() {
        every { repository.existsByKeycloakIdAndLocationId(keycloakId, locationId) } returns true

        assertThrows<AssignmentAlreadyExistsException> {
            service.assignLocationAdmin(keycloakId, locationId)
        }
    }

    @Test
    fun `removeAssignment deletes existing assignment`() {
        every { repository.existsByKeycloakIdAndLocationId(keycloakId, locationId) } returns true
        every { repository.deleteByKeycloakIdAndLocationId(keycloakId, locationId) } returns Unit

        service.removeAssignment(keycloakId, locationId)

        verify { repository.deleteByKeycloakIdAndLocationId(keycloakId, locationId) }
    }

    @Test
    fun `removeAssignment throws when assignment not found`() {
        every { repository.existsByKeycloakIdAndLocationId(keycloakId, locationId) } returns false

        assertThrows<AssignmentNotFoundException> {
            service.removeAssignment(keycloakId, locationId)
        }
    }

    @Test
    fun `getAdminLocationIds returns location IDs for keycloak ID`() {
        val locationId2 = UUID.randomUUID()
        every { repository.findByKeycloakId(keycloakId) } returns listOf(
            LocationAdminAssignment(id = UUID.randomUUID(), keycloakId = keycloakId, locationId = locationId, createdAt = Instant.now()),
            LocationAdminAssignment(id = UUID.randomUUID(), keycloakId = keycloakId, locationId = locationId2, createdAt = Instant.now()),
        )

        val result = service.getAdminLocationIds(keycloakId)

        assertEquals(2, result.size)
        assertTrue(result.contains(locationId))
        assertTrue(result.contains(locationId2))
    }
}
