package com.nickdferrara.fitify.admin.internal.dtos.response

import java.util.UUID

internal data class CancelClassResponse(
    val classId: UUID,
    val className: String,
    val affectedBookings: Int,
    val affectedWaitlist: Int,
)
