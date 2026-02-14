package com.nickdferrara.fitify.admin.internal.exceptions

import java.util.UUID

internal class CoachScheduleConflictException(coachId: UUID, details: String) :
    RuntimeException("Coach $coachId has a schedule conflict: $details")

internal class LocationNotFoundException(locationId: UUID) :
    RuntimeException("Location not found: $locationId")

internal class CoachNotFoundException(coachId: UUID) :
    RuntimeException("Coach not found: $coachId")

internal class ClassNotFoundException(classId: UUID) :
    RuntimeException("Class not found: $classId")

internal class InvalidRecurringScheduleException(reason: String) :
    RuntimeException("Invalid recurring schedule: $reason")
