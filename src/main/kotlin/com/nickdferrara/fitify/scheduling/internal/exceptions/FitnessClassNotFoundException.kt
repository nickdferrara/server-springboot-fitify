package com.nickdferrara.fitify.scheduling.internal.exceptions

import java.util.UUID

internal class FitnessClassNotFoundException(id: UUID) :
    RuntimeException("Fitness class not found: $id")
