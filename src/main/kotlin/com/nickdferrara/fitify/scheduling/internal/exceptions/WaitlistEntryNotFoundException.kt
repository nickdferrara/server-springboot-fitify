package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.util.UUID

internal class WaitlistEntryNotFoundException(classId: UUID, userId: UUID) :
    RuntimeException("Waitlist entry not found for class $classId and user $userId")
