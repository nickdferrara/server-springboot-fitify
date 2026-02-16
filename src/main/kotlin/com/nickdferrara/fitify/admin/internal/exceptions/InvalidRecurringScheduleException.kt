package com.nickdferrara.fitify.admin.internal.exceptions

internal class InvalidRecurringScheduleException(reason: String) :
    RuntimeException("Invalid recurring schedule: $reason")
