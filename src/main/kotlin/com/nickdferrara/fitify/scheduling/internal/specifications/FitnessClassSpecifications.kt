package com.nickdferrara.fitify.scheduling.internal.specifications

import com.nickdferrara.fitify.scheduling.internal.entities.Booking
import com.nickdferrara.fitify.scheduling.internal.enums.BookingStatus
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import com.nickdferrara.fitify.scheduling.internal.enums.FitnessClassStatus
import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

internal object FitnessClassSpecifications {

    fun hasDate(date: LocalDate): Specification<FitnessClass> {
        val dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val dayEnd = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        return Specification { root, _, cb ->
            cb.and(
                cb.greaterThanOrEqualTo(root.get("startTime"), dayStart),
                cb.lessThan(root.get("startTime"), dayEnd),
            )
        }
    }

    fun hasClassType(classType: String): Specification<FitnessClass> =
        Specification { root, _, cb ->
            cb.equal(cb.lower(root.get("classType")), classType.lowercase())
        }

    fun hasCoach(coachId: UUID): Specification<FitnessClass> =
        Specification { root, _, cb ->
            cb.equal(root.get<UUID>("coachId"), coachId)
        }

    fun hasLocation(locationId: UUID): Specification<FitnessClass> =
        Specification { root, _, cb ->
            cb.equal(root.get<UUID>("locationId"), locationId)
        }

    fun hasAvailability(): Specification<FitnessClass> =
        Specification { root, query, cb ->
            val bookingJoin = root.join<FitnessClass, Booking>("bookings", JoinType.LEFT)
            bookingJoin.on(cb.equal(bookingJoin.get<BookingStatus>("status"), BookingStatus.CONFIRMED))
            query?.groupBy(root.get<UUID>("id"))
            query?.having(cb.lt(cb.count(bookingJoin.get<UUID>("id")), root.get("capacity")))
            cb.conjunction()
        }

    fun isActive(): Specification<FitnessClass> =
        Specification { root, _, cb ->
            cb.equal(root.get<FitnessClassStatus>("status"), FitnessClassStatus.ACTIVE)
        }

    fun isFuture(): Specification<FitnessClass> =
        Specification { root, _, cb ->
            cb.greaterThan(root.get("startTime"), Instant.now())
        }

    fun combine(vararg specs: Specification<FitnessClass>?): Specification<FitnessClass> {
        return specs.filterNotNull().reduceOrNull { acc, spec -> acc.and(spec) }
            ?: Specification.where(null)
    }
}
