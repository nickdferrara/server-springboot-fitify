package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.util.UUID

internal class CancellationWindowClosedException(classId: UUID, windowHours: Long) :
    RuntimeException("Cancellation window of ${windowHours}h has closed for class $classId")
