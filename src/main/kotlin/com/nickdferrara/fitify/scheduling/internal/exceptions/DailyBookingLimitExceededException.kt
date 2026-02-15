package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.util.UUID

internal class DailyBookingLimitExceededException(userId: UUID, limit: Int) :
    RuntimeException("User $userId has reached the daily booking limit of $limit")
