package com.nickdferrara.fitify.admin.internal.config

import com.nickdferrara.fitify.admin.internal.entities.BusinessRule
import com.nickdferrara.fitify.admin.internal.repository.BusinessRuleRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
internal class BusinessRuleInitializer(
    private val businessRuleRepository: BusinessRuleRepository,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val defaults = listOf(
            Triple("cancellation_window_hours", "24", "Hours before class start when cancellation is no longer allowed"),
            Triple("max_waitlist_size", "20", "Maximum number of users on a class waitlist"),
            Triple("password_reset_token_expiry_minutes", "15", "Minutes before a password reset token expires"),
            Triple("subscription_grace_period_days", "7", "Days after subscription expiry before access is revoked"),
            Triple("discount_stacking_enabled", "false", "Whether multiple discounts can be applied to a single subscription"),
            Triple("max_bookings_per_user_per_day", "3", "Maximum number of class bookings a user can make per day"),
        )

        for ((ruleKey, value, description) in defaults) {
            if (businessRuleRepository.findByRuleKeyAndLocationIdIsNull(ruleKey) == null) {
                businessRuleRepository.save(
                    BusinessRule(
                        ruleKey = ruleKey,
                        value = value,
                        description = description,
                        updatedBy = "system",
                    ),
                )
            }
        }
    }
}
