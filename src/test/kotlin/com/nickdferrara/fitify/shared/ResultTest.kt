package com.nickdferrara.fitify.shared

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ResultTest {

    @Test
    fun `Success holds value`() {
        val result: Result<String, DomainError> = Result.Success("hello")
        assertIs<Result.Success<String>>(result)
        assertEquals("hello", result.value)
    }

    @Test
    fun `Failure holds error`() {
        val result: Result<String, DomainError> = Result.Failure(NotFoundError("not found"))
        assertIs<Result.Failure<DomainError>>(result)
        assertEquals("not found", result.error.message)
    }

    @Test
    fun `map transforms Success value`() {
        val result: Result<Int, DomainError> = Result.Success(5)
        val mapped = result.map { it * 2 }
        assertIs<Result.Success<Int>>(mapped)
        assertEquals(10, mapped.value)
    }

    @Test
    fun `map does not transform Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(ValidationError("invalid"))
        val mapped = result.map { it * 2 }
        assertIs<Result.Failure<DomainError>>(mapped)
        assertEquals("invalid", mapped.error.message)
    }

    @Test
    fun `flatMap chains Success`() {
        val result: Result<Int, DomainError> = Result.Success(5)
        val chained = result.flatMap { Result.Success(it.toString()) }
        assertIs<Result.Success<String>>(chained)
        assertEquals("5", chained.value)
    }

    @Test
    fun `flatMap short-circuits on Failure`() {
        val result: Result<Int, DomainError> = Result.Failure(ConflictError("conflict"))
        val chained = result.flatMap { Result.Success(it.toString()) }
        assertIs<Result.Failure<DomainError>>(chained)
        assertEquals("conflict", chained.error.message)
    }

    @Test
    fun `mapError transforms Failure error`() {
        val result: Result<Int, DomainError> = Result.Failure(NotFoundError("missing"))
        val mapped = result.mapError { UnauthorizedError("denied") }
        assertIs<Result.Failure<DomainError>>(mapped)
        assertEquals("denied", mapped.error.message)
    }

    @Test
    fun `mapError does not affect Success`() {
        val result: Result<Int, DomainError> = Result.Success(42)
        val mapped = result.mapError { UnauthorizedError("denied") }
        assertIs<Result.Success<Int>>(mapped)
        assertEquals(42, mapped.value)
    }

    @Test
    fun `getOrNull returns value for Success`() {
        val result: Result<String, DomainError> = Result.Success("value")
        assertEquals("value", result.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Failure`() {
        val result: Result<String, DomainError> = Result.Failure(NotFoundError("missing"))
        assertNull(result.getOrNull())
    }

    @Test
    fun `getOrElse returns value for Success`() {
        val result: Result<String, DomainError> = Result.Success("value")
        assertEquals("value", result.getOrElse { "default" })
    }

    @Test
    fun `getOrElse returns default for Failure`() {
        val result: Result<String, DomainError> = Result.Failure(NotFoundError("missing"))
        assertEquals("default", result.getOrElse { "default" })
    }
}
