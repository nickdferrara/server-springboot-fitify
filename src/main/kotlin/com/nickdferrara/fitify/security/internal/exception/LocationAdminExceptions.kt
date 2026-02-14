package com.nickdferrara.fitify.security.internal.exception

internal class AssignmentAlreadyExistsException(keycloakId: String, locationId: java.util.UUID) :
    RuntimeException("Assignment already exists for user $keycloakId at location $locationId")

internal class AssignmentNotFoundException(keycloakId: String, locationId: java.util.UUID) :
    RuntimeException("Assignment not found for user $keycloakId at location $locationId")
