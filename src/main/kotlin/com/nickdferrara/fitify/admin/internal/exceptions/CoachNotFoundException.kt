package com.nickdferrara.fitify.admin.internal.exceptions

import java.util.UUID

internal class CoachNotFoundException(coachId: UUID) :
    RuntimeException("Coach not found: $coachId")
