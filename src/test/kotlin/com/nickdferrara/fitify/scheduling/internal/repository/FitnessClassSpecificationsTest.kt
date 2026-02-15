package com.nickdferrara.fitify.scheduling.internal.repository

import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import com.nickdferrara.fitify.scheduling.internal.specifications.FitnessClassSpecifications
import com.nickdferrara.fitify.scheduling.internal.enums.FitnessClassStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

internal class FitnessClassSpecificationsTest {

    private val root = mockk<Root<FitnessClass>>(relaxed = true)
    private val query = mockk<CriteriaQuery<*>>(relaxed = true)
    private val cb = mockk<CriteriaBuilder>(relaxed = true)
    private val predicate = mockk<Predicate>(relaxed = true)

    @Test
    fun `hasDate creates predicate for day boundaries`() {
        val date = LocalDate.of(2025, 6, 15)
        val dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val dayEnd = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val startPath = mockk<Path<Any>>(relaxed = true)
        every { root.get<Any>("startTime") } returns startPath
        every { cb.greaterThanOrEqualTo(any<Path<Comparable<Any>>>(), any<Comparable<Any>>()) } returns predicate
        every { cb.lessThan(any<Path<Comparable<Any>>>(), any<Comparable<Any>>()) } returns predicate
        every { cb.and(any(), any()) } returns predicate

        val spec = FitnessClassSpecifications.hasDate(date)
        spec.toPredicate(root, query, cb)

        verify { cb.and(any(), any()) }
    }

    @Test
    fun `hasClassType creates case-insensitive predicate`() {
        val typePath = mockk<Path<Any>>(relaxed = true)
        every { root.get<Any>("classType") } returns typePath
        every { cb.lower(any()) } returns mockk(relaxed = true)
        every { cb.equal(any(), any()) } returns predicate

        val spec = FitnessClassSpecifications.hasClassType("YOGA")
        spec.toPredicate(root, query, cb)

        verify { cb.lower(any()) }
        verify { cb.equal(any(), "yoga") }
    }

    @Test
    fun `hasCoach creates equality predicate on coachId`() {
        val coachId = UUID.randomUUID()
        val coachPath = mockk<Path<UUID>>(relaxed = true)
        every { root.get<UUID>("coachId") } returns coachPath
        every { cb.equal(any(), coachId) } returns predicate

        val spec = FitnessClassSpecifications.hasCoach(coachId)
        spec.toPredicate(root, query, cb)

        verify { cb.equal(coachPath, coachId) }
    }

    @Test
    fun `hasLocation creates equality predicate on locationId`() {
        val locationId = UUID.randomUUID()
        val locationPath = mockk<Path<UUID>>(relaxed = true)
        every { root.get<UUID>("locationId") } returns locationPath
        every { cb.equal(any(), locationId) } returns predicate

        val spec = FitnessClassSpecifications.hasLocation(locationId)
        spec.toPredicate(root, query, cb)

        verify { cb.equal(locationPath, locationId) }
    }

    @Test
    fun `isActive creates predicate for ACTIVE status`() {
        val statusPath = mockk<Path<FitnessClassStatus>>(relaxed = true)
        every { root.get<FitnessClassStatus>("status") } returns statusPath
        every { cb.equal(any(), FitnessClassStatus.ACTIVE) } returns predicate

        val spec = FitnessClassSpecifications.isActive()
        spec.toPredicate(root, query, cb)

        verify { cb.equal(statusPath, FitnessClassStatus.ACTIVE) }
    }

    @Test
    fun `isFuture creates greater-than predicate on startTime`() {
        val startTimePath = mockk<Path<Any>>(relaxed = true)
        every { root.get<Any>("startTime") } returns startTimePath
        every { cb.greaterThan(any<Path<Comparable<Any>>>(), any<Comparable<Any>>()) } returns predicate

        val spec = FitnessClassSpecifications.isFuture()
        spec.toPredicate(root, query, cb)

        verify { cb.greaterThan(any<Path<Comparable<Any>>>(), any<Comparable<Any>>()) }
    }

    @Test
    fun `combine merges multiple non-null specifications`() {
        val spec1 = FitnessClassSpecifications.isActive()
        val spec2 = FitnessClassSpecifications.isFuture()

        val combined = FitnessClassSpecifications.combine(spec1, spec2)

        assertThat(combined).isNotNull
    }

    @Test
    fun `combine filters out null specifications`() {
        val spec1 = FitnessClassSpecifications.isActive()

        val combined = FitnessClassSpecifications.combine(spec1, null, null)

        assertThat(combined).isNotNull
    }

    @Test
    fun `combine returns where-null specification when all inputs are null`() {
        val combined = FitnessClassSpecifications.combine(null, null)

        assertThat(combined).isNotNull
    }
}
