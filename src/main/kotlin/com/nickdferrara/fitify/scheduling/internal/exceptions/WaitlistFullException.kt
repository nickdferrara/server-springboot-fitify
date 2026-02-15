package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.util.UUID

internal class WaitlistFullException(classId: UUID) :
    RuntimeException("Waitlist is full for class $classId")
