package com.nickdferrara.fitify.scheduling.internal.service

import org.springframework.stereotype.Component

@Component
internal class SchedulingEventListener {

    // Placeholder for cross-module event handling.
    // Future listeners:
    // - BusinessRuleUpdatedEvent: update cancellationWindowHours, maxWaitlistSize, maxBookingsPerDay
    // - SubscriptionExpiredEvent: prevent bookings for expired subscriptions
    // - LocationDeactivatedEvent: cancel all future classes at deactivated location
}
