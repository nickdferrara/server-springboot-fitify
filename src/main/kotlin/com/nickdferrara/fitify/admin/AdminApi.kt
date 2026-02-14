package com.nickdferrara.fitify.admin

import java.util.UUID

interface AdminApi {
    fun getBusinessRuleValue(ruleKey: String, locationId: UUID? = null): String?
}
