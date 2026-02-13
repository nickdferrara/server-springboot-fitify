package com.nickdferrara.fitify.shared

interface DomainError {
    val message: String
}

data class NotFoundError(override val message: String) : DomainError

data class ValidationError(override val message: String) : DomainError

data class ConflictError(override val message: String) : DomainError

data class UnauthorizedError(override val message: String) : DomainError
