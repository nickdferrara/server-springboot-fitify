package com.nickdferrara.fitify.scheduling

import java.util.UUID

data class ClassFullEvent(
    val classId: UUID,
    val className: String,
    val waitlistSize: Int,
)
