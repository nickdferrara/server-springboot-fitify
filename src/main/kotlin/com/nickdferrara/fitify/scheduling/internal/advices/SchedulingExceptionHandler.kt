package com.nickdferrara.fitify.scheduling.internal.advices

import com.nickdferrara.fitify.scheduling.internal.controller.ClassController
import com.nickdferrara.fitify.scheduling.internal.controller.WaitlistController
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ErrorResponse
import com.nickdferrara.fitify.scheduling.internal.dtos.response.ValidationErrorResponse
import com.nickdferrara.fitify.scheduling.internal.exceptions.AlreadyBookedException
import com.nickdferrara.fitify.scheduling.internal.exceptions.BookingNotFoundException
import com.nickdferrara.fitify.scheduling.internal.exceptions.CancellationWindowClosedException
import com.nickdferrara.fitify.scheduling.internal.exceptions.ClassNotBookableException
import com.nickdferrara.fitify.scheduling.internal.exceptions.DailyBookingLimitExceededException
import com.nickdferrara.fitify.scheduling.internal.exceptions.FitnessClassNotFoundException
import com.nickdferrara.fitify.scheduling.internal.exceptions.ScheduleConflictException
import com.nickdferrara.fitify.scheduling.internal.exceptions.WaitlistEntryNotFoundException
import com.nickdferrara.fitify.scheduling.internal.exceptions.WaitlistFullException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(
    assignableTypes = [
        ClassController::class,
        WaitlistController::class,
    ]
)
internal class SchedulingExceptionHandler {

    @ExceptionHandler(FitnessClassNotFoundException::class)
    fun handleClassNotFound(ex: FitnessClassNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Fitness class not found"))
    }

    @ExceptionHandler(BookingNotFoundException::class)
    fun handleBookingNotFound(ex: BookingNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Booking not found"))
    }

    @ExceptionHandler(WaitlistEntryNotFoundException::class)
    fun handleWaitlistEntryNotFound(ex: WaitlistEntryNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Waitlist entry not found"))
    }

    @ExceptionHandler(ScheduleConflictException::class)
    fun handleScheduleConflict(ex: ScheduleConflictException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.message ?: "Schedule conflict"))
    }

    @ExceptionHandler(AlreadyBookedException::class)
    fun handleAlreadyBooked(ex: AlreadyBookedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(ex.message ?: "Already booked"))
    }

    @ExceptionHandler(ClassNotBookableException::class)
    fun handleClassNotBookable(ex: ClassNotBookableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Class not bookable"))
    }

    @ExceptionHandler(CancellationWindowClosedException::class)
    fun handleCancellationWindowClosed(ex: CancellationWindowClosedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Cancellation window closed"))
    }

    @ExceptionHandler(WaitlistFullException::class)
    fun handleWaitlistFull(ex: WaitlistFullException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Waitlist full"))
    }

    @ExceptionHandler(DailyBookingLimitExceededException::class)
    fun handleDailyBookingLimit(ex: DailyBookingLimitExceededException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Daily booking limit exceeded"))
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleOptimisticLocking(ex: ObjectOptimisticLockingFailureException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse("Concurrent modification detected. Please retry."))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ValidationErrorResponse("Validation failed", errors))
    }
}
