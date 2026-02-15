package com.nickdferrara.fitify.admin.internal.listener

import com.nickdferrara.fitify.admin.internal.entities.MetricType
import com.nickdferrara.fitify.admin.internal.entities.MetricsSnapshot
import com.nickdferrara.fitify.admin.internal.repository.MetricsSnapshotRepository
import com.nickdferrara.fitify.identity.UserRegisteredEvent
import com.nickdferrara.fitify.location.LocationCreatedEvent
import com.nickdferrara.fitify.notification.NotificationSentEvent
import com.nickdferrara.fitify.scheduling.BookingCancelledEvent
import com.nickdferrara.fitify.scheduling.ClassSummary
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.scheduling.WaitlistPromotedEvent
import com.nickdferrara.fitify.shared.NotFoundError
import com.nickdferrara.fitify.shared.Result
import com.nickdferrara.fitify.subscription.SubscriptionCreatedEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class MetricsEventListenerTest {

    private val repository = mockk<MetricsSnapshotRepository>()
    private val schedulingApi = mockk<SchedulingApi>()
    private val listener = MetricsEventListener(repository, schedulingApi)

    private val classId = UUID.randomUUID()
    private val locationId = UUID.randomUUID()

    private fun buildClassSummary() = ClassSummary(
        id = classId,
        name = "Morning Yoga",
        description = null,
        classType = "yoga",
        startTime = Instant.now(),
        endTime = Instant.now().plusSeconds(3600),
        locationId = locationId,
        coachId = UUID.randomUUID(),
    )

    @Test
    fun `onUserRegistered creates signup snapshot`() {
        val event = UserRegisteredEvent(UUID.randomUUID(), "test@test.com", "John", "Doe")

        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.SIGNUPS, any())
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        listener.onUserRegistered(event)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.SIGNUPS &&
                    it.value == BigDecimal.ONE &&
                    it.locationId == null
            })
        }
    }

    @Test
    fun `onUserRegistered increments existing snapshot`() {
        val event = UserRegisteredEvent(UUID.randomUUID(), "test@test.com", "John", "Doe")
        val existingSnapshot = MetricsSnapshot(
            id = UUID.randomUUID(),
            metricType = MetricType.SIGNUPS,
            value = BigDecimal(5),
            snapshotDate = LocalDate.now(),
        )

        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.SIGNUPS, any())
        } returns listOf(existingSnapshot)
        every { repository.save(any()) } answers { firstArg() }

        listener.onUserRegistered(event)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.value == BigDecimal(6)
            })
        }
    }

    @Test
    fun `onBookingCancelled creates cancellation snapshot with location`() {
        val event = BookingCancelledEvent(UUID.randomUUID(), classId, Instant.now())

        every { schedulingApi.findClassById(classId) } returns Result.Success(buildClassSummary())
        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationId(MetricType.CANCELLATIONS, any(), locationId)
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        listener.onBookingCancelled(event)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.CANCELLATIONS &&
                    it.locationId == locationId &&
                    it.dimensions == mapOf("class_type" to "yoga") &&
                    it.value == BigDecimal.ONE
            })
        }
    }

    @Test
    fun `onBookingCancelled falls back to global when class not found`() {
        val event = BookingCancelledEvent(UUID.randomUUID(), classId, Instant.now())

        every { schedulingApi.findClassById(classId) } returns Result.Failure(NotFoundError("not found"))
        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.CANCELLATIONS, any())
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        listener.onBookingCancelled(event)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.CANCELLATIONS &&
                    it.locationId == null
            })
        }
    }

    @Test
    fun `onSubscriptionCreated creates revenue snapshot with plan type dimension`() {
        val event = SubscriptionCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "MONTHLY", "sub_123", Instant.now().plusSeconds(86400))

        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.REVENUE, any())
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        listener.onSubscriptionCreated(event)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.REVENUE &&
                    it.dimensions == mapOf("plan_type" to "MONTHLY") &&
                    it.value == BigDecimal.ONE
            })
        }
    }

    @Test
    fun `onWaitlistPromoted creates waitlist conversion snapshot with location`() {
        val event = WaitlistPromotedEvent(UUID.randomUUID(), classId, "Morning Yoga", Instant.now())

        every { schedulingApi.findClassById(classId) } returns Result.Success(buildClassSummary())
        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationId(MetricType.WAITLIST_CONVERSION, any(), locationId)
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        listener.onWaitlistPromoted(event)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.WAITLIST_CONVERSION &&
                    it.locationId == locationId &&
                    it.dimensions == mapOf("class_type" to "yoga")
            })
        }
    }

    @Test
    fun `onLocationCreated creates location snapshot`() {
        val newLocationId = UUID.randomUUID()
        val event = LocationCreatedEvent(newLocationId, "Downtown Gym", "123 Main St", "America/New_York")

        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationId(MetricType.LOCATIONS, any(), newLocationId)
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        listener.onLocationCreated(event)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.LOCATIONS &&
                    it.locationId == newLocationId &&
                    it.dimensions == mapOf("time_zone" to "America/New_York") &&
                    it.value == BigDecimal.ONE
            })
        }
    }

    @Test
    fun `onNotificationSent creates notification snapshot`() {
        val event = NotificationSentEvent(UUID.randomUUID(), "EMAIL", "DELIVERED")

        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.NOTIFICATIONS_SENT, any())
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        listener.onNotificationSent(event)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.NOTIFICATIONS_SENT &&
                    it.locationId == null &&
                    it.dimensions == mapOf("channel" to "EMAIL", "status" to "DELIVERED") &&
                    it.value == BigDecimal.ONE
            })
        }
    }
}
