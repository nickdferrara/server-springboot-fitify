package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.util.UUID

internal class AlreadyBookedException(classId: UUID, userId: UUID) :
    RuntimeException("User $userId is already booked for class $classId")
