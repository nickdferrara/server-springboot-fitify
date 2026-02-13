package com.nickdferrara.fitify.scheduling.internal.repository

import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.time.Instant
import java.util.UUID

internal interface FitnessClassRepository :
    JpaRepository<FitnessClass, UUID>,
    JpaSpecificationExecutor<FitnessClass> {

    fun findByLocationIdAndStartTimeAfterOrderByStartTimeAsc(
        locationId: UUID,
        after: Instant,
    ): List<FitnessClass>
}
