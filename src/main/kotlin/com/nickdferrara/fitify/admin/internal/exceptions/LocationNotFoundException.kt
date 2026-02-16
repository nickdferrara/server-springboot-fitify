package com.nickdferrara.fitify.admin.internal.exceptions

import java.util.UUID

internal class LocationNotFoundException(locationId: UUID) :
    RuntimeException("Location not found: $locationId")
