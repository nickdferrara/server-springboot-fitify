package com.nickdferrara.fitify.subscription.internal.repository

import com.nickdferrara.fitify.subscription.internal.entities.DiscountStrategy
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface DiscountStrategyRepository : JpaRepository<DiscountStrategy, UUID> {
    fun findByActiveTrueOrderByPriorityAsc(): List<DiscountStrategy>
}
