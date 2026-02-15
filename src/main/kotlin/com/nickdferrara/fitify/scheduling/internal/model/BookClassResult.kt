package com.nickdferrara.fitify.scheduling.internal.model

import com.nickdferrara.fitify.scheduling.internal.dtos.response.BookingResponse
import com.nickdferrara.fitify.scheduling.internal.dtos.response.WaitlistEntryResponse

internal sealed class BookClassResult {
    data class Booked(val booking: BookingResponse) : BookClassResult()
    data class Waitlisted(val waitlistEntry: WaitlistEntryResponse) : BookClassResult()
}
