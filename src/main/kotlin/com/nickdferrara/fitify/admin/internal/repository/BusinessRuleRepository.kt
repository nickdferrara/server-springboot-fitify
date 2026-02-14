package com.nickdferrara.fitify.admin.internal.repository

import com.nickdferrara.fitify.admin.internal.entities.BusinessRule
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface BusinessRuleRepository : JpaRepository<BusinessRule, UUID> {
    fun findByRuleKeyAndLocationIdIsNull(ruleKey: String): BusinessRule?
    fun findByRuleKeyAndLocationId(ruleKey: String, locationId: UUID): BusinessRule?
    fun findAllByOrderByRuleKeyAscLocationIdAsc(): List<BusinessRule>
}
