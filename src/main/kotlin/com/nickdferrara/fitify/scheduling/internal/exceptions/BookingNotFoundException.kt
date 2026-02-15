package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.util.UUID

internal class BookingNotFoundException(classId: UUID, userId: UUID) :
    RuntimeException("Booking not found for class $classId and user $userId")
