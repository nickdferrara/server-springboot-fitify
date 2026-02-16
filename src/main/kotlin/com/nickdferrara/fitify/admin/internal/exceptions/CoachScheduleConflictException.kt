package com.nickdferrara.fitify.admin.internal.exceptions

import java.util.UUID

internal class CoachScheduleConflictException(coachId: UUID, details: String) :
    RuntimeException("Coach $coachId has a schedule conflict: $details")
