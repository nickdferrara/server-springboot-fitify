package com.nickdferrara.fitify.admin.internal.exceptions

internal class InvalidMetricsQueryException(reason: String) :
    RuntimeException("Invalid metrics query: $reason")
