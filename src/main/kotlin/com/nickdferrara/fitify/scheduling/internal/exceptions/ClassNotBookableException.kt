package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.util.UUID

internal class ClassNotBookableException(classId: UUID, reason: String) :
    RuntimeException("Class $classId is not bookable: $reason")
