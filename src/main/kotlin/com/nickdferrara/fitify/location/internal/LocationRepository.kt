package com.nickdferrara.fitify.location.internal

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface LocationRepository : JpaRepository<Location, UUID> {
    fun findByActiveTrue(): List<Location>
}
