package com.nickdferrara.fitify.admin.internal.exceptions

import java.util.UUID

internal class ClassNotFoundException(classId: UUID) :
    RuntimeException("Class not found: $classId")
