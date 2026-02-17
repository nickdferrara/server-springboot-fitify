package com.nickdferrara.fitify.scheduling.internal.listener

import com.nickdferrara.fitify.scheduling.internal.service.interfaces.SchedulingCommandService
import com.nickdferrara.fitify.shared.BusinessRuleUpdatedEvent
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class BusinessRuleEventListenerTest {

    private val schedulingCommandService = mockk<SchedulingCommandService>(relaxed = true)
    private val listener = BusinessRuleEventListener(schedulingCommandService)

    @Test
    fun `onBusinessRuleUpdated updates cancellation window hours`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "cancellation_window_hours",
            newValue = "48",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)

        verify { schedulingCommandService.cancellationWindowHours = 48L }
    }

    @Test
    fun `onBusinessRuleUpdated updates max waitlist size`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "max_waitlist_size",
            newValue = "30",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)

        verify { schedulingCommandService.maxWaitlistSize = 30 }
    }

    @Test
    fun `onBusinessRuleUpdated updates max bookings per day`() {
        val event = BusinessRuleUpdatedEvent(
            ruleKey = "max_bookings_per_user_per_day",
            newValue = "5",
            locationId = null,
            updatedBy = "admin",
        )

        listener.onBusinessRuleUpdated(event)

        verify { schedulingCommandService.maxBookingsPerDay = 5 }
    }
}
