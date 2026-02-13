package com.nickdferrara.fitify.coaching.internal.service

import com.nickdferrara.fitify.coaching.CoachCreatedEvent
import com.nickdferrara.fitify.coaching.CoachDeactivatedEvent
import com.nickdferrara.fitify.coaching.CoachSummary
import com.nickdferrara.fitify.coaching.CoachUpdatedEvent
import com.nickdferrara.fitify.coaching.CoachingApi
import com.nickdferrara.fitify.coaching.internal.dtos.request.AssignCoachLocationsRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.CreateCoachRequest
import com.nickdferrara.fitify.coaching.internal.dtos.request.UpdateCoachRequest
import com.nickdferrara.fitify.coaching.internal.dtos.response.CoachResponse
import com.nickdferrara.fitify.coaching.internal.dtos.response.toResponse
import com.nickdferrara.fitify.coaching.internal.entities.Coach
import com.nickdferrara.fitify.coaching.internal.entities.CoachCertification
import com.nickdferrara.fitify.coaching.internal.entities.CoachLocation
import com.nickdferrara.fitify.coaching.internal.repository.CoachRepository
import com.nickdferrara.fitify.shared.DomainError
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
internal class CoachingService(
    private val coachRepository: CoachRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : CoachingApi {

    override fun findCoachById(id: UUID): Result<CoachSummary, DomainError> {
        val coach = coachRepository.findById(id).orElse(null)
            ?: return Result.Failure(NotFoundError("Coach not found: $id"))
        return Result.Success(coach.toSummary())
    }

    override fun findAllActiveCoaches(): List<CoachSummary> {
        return coachRepository.findByActiveTrue().map { it.toSummary() }
    }

    override fun findActiveCoachesByLocationId(locationId: UUID): List<CoachSummary> {
        return coachRepository.findActiveCoachesByLocationId(locationId).map { it.toSummary() }
    }

    fun findAll(): List<CoachResponse> {
        return coachRepository.findAll().map { it.toResponse() }
    }

    fun findById(id: UUID): CoachResponse {
        val coach = coachRepository.findById(id)
            .orElseThrow { CoachNotFoundException(id) }
        return coach.toResponse()
    }

    @Transactional
    fun createCoach(request: CreateCoachRequest): CoachResponse {
        val coach = Coach(
            name = request.name,
            bio = request.bio,
            photoUrl = request.photoUrl,
            specializations = request.specializations,
        )

        request.certifications.forEach { cert ->
            coach.certifications.add(
                CoachCertification(
                    coach = coach,
                    name = cert.name,
                    issuer = cert.issuer,
                    validUntil = cert.validUntil,
                )
            )
        }

        val saved = coachRepository.save(coach)

        eventPublisher.publishEvent(
            CoachCreatedEvent(
                coachId = saved.id!!,
                name = saved.name,
            )
        )

        return saved.toResponse()
    }

    @Transactional
    fun updateCoach(id: UUID, request: UpdateCoachRequest): CoachResponse {
        val coach = coachRepository.findById(id)
            .orElseThrow { CoachNotFoundException(id) }

        val updatedFields = mutableSetOf<String>()

        request.name?.let { coach.name = it; updatedFields.add("name") }
        request.bio?.let { coach.bio = it; updatedFields.add("bio") }
        request.photoUrl?.let { coach.photoUrl = it; updatedFields.add("photoUrl") }
        request.specializations?.let { coach.specializations = it; updatedFields.add("specializations") }

        request.certifications?.let { certList ->
            coach.certifications.clear()
            certList.forEach { cert ->
                coach.certifications.add(
                    CoachCertification(
                        coach = coach,
                        name = cert.name,
                        issuer = cert.issuer,
                        validUntil = cert.validUntil,
                    )
                )
            }
            updatedFields.add("certifications")
        }

        val saved = coachRepository.save(coach)

        if (updatedFields.isNotEmpty()) {
            eventPublisher.publishEvent(
                CoachUpdatedEvent(
                    coachId = saved.id!!,
                    updatedFields = updatedFields,
                )
            )
        }

        return saved.toResponse()
    }

    @Transactional
    fun deactivateCoach(id: UUID) {
        val coach = coachRepository.findById(id)
            .orElseThrow { CoachNotFoundException(id) }

        coach.active = false
        coachRepository.save(coach)

        eventPublisher.publishEvent(
            CoachDeactivatedEvent(
                coachId = coach.id!!,
                effectiveDate = Instant.now(),
            )
        )
    }

    @Transactional
    fun assignLocations(coachId: UUID, request: AssignCoachLocationsRequest): CoachResponse {
        val coach = coachRepository.findById(coachId)
            .orElseThrow { CoachNotFoundException(coachId) }

        coach.locations.clear()
        request.locationIds.forEach { locationId ->
            coach.locations.add(
                CoachLocation(
                    coach = coach,
                    locationId = locationId,
                )
            )
        }

        val saved = coachRepository.save(coach)
        return saved.toResponse()
    }

    fun findCoachesByLocationId(locationId: UUID): List<CoachResponse> {
        return coachRepository.findActiveCoachesByLocationId(locationId).map { it.toResponse() }
    }

    private fun Coach.toSummary() = CoachSummary(
        id = id!!,
        name = name,
        active = active,
    )
}

internal class CoachNotFoundException(id: UUID) :
    RuntimeException("Coach not found: $id")
