package com.nickdferrara.fitify.scheduling

import java.util.UUID

data class ClassUpdatedEvent(
    val classId: UUID,
    val className: String,
    val locationId: UUID,
    val updatedFields: List<String>,
    val affectedUserIds: List<UUID>,
)
