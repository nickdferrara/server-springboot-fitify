package com.nickdferrara.fitify.admin.internal.scheduler

import com.nickdferrara.fitify.admin.internal.entities.MetricType
import com.nickdferrara.fitify.admin.internal.entities.MetricsSnapshot
import com.nickdferrara.fitify.admin.internal.repository.MetricsSnapshotRepository
import com.nickdferrara.fitify.scheduling.ClassUtilizationSummary
import com.nickdferrara.fitify.scheduling.SchedulingApi
import com.nickdferrara.fitify.subscription.SubscriptionApi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class MetricsAggregationSchedulerTest {

    private val repository = mockk<MetricsSnapshotRepository>()
    private val subscriptionApi = mockk<SubscriptionApi>()
    private val schedulingApi = mockk<SchedulingApi>()
    private val scheduler = MetricsAggregationScheduler(repository, subscriptionApi, schedulingApi)

    private val locationId = UUID.randomUUID()

    @Test
    fun `aggregateActiveSubscriptions stores counts by plan type and total`() {
        val date = LocalDate.of(2025, 6, 15)

        every { subscriptionApi.countActiveSubscriptionsByPlanType() } returns mapOf(
            "MONTHLY" to 80L,
            "ANNUAL" to 20L,
        )
        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.ACTIVE_SUBSCRIPTIONS, date)
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        scheduler.aggregateActiveSubscriptions(date)

        verify(exactly = 3) { repository.save(any()) }
        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.ACTIVE_SUBSCRIPTIONS &&
                    it.dimensions.isEmpty() &&
                    it.value == BigDecimal(100)
            })
        }
    }

    @Test
    fun `aggregateRevenue stores revenue by plan type and total`() {
        val date = LocalDate.of(2025, 6, 15)

        every { subscriptionApi.sumRevenueBetweenByPlanType(any(), any()) } returns mapOf(
            "MONTHLY" to BigDecimal("499.00"),
            "ANNUAL" to BigDecimal("1999.00"),
        )
        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.REVENUE, date)
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        scheduler.aggregateRevenue(date)

        verify(exactly = 3) { repository.save(any()) }
        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.REVENUE &&
                    it.dimensions.isEmpty() &&
                    it.value.compareTo(BigDecimal("2498.00")) == 0
            })
        }
    }

    @Test
    fun `aggregateClassUtilization computes average utilization per location`() {
        val date = LocalDate.of(2025, 6, 15)

        every { schedulingApi.getClassUtilizationByDateRange(any(), any()) } returns listOf(
            ClassUtilizationSummary(UUID.randomUUID(), locationId, "yoga", 20, 16),
            ClassUtilizationSummary(UUID.randomUUID(), locationId, "hiit", 20, 20),
        )
        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationId(MetricType.CLASS_UTILIZATION, date, locationId)
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        scheduler.aggregateClassUtilization(date)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.CLASS_UTILIZATION &&
                    it.locationId == locationId &&
                    it.value.toDouble() > 85.0 // (80% + 100%) / 2 = 90%
            })
        }
    }

    @Test
    fun `aggregateChurnRate only runs on first day of month`() {
        val nonFirstDay = LocalDate.of(2025, 6, 15)

        scheduler.aggregateChurnRate(nonFirstDay)

        verify(exactly = 0) { subscriptionApi.countSubscriptionsExpiredBetween(any(), any()) }
    }

    @Test
    fun `aggregateChurnRate computes rate on first day of month`() {
        val firstOfMonth = LocalDate.of(2025, 7, 1)

        every { subscriptionApi.countSubscriptionsExpiredBetween(any(), any()) } returns 5
        every { subscriptionApi.countActiveSubscriptionsAsOf(any()) } returns 100
        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.CHURN_RATE, any())
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        scheduler.aggregateChurnRate(firstOfMonth)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.CHURN_RATE &&
                    it.value.toDouble() == 5.0 // 5/100 * 100 = 5.0%
            })
        }
    }

    @Test
    fun `aggregateChurnRate returns zero when no active subscriptions`() {
        val firstOfMonth = LocalDate.of(2025, 7, 1)

        every { subscriptionApi.countSubscriptionsExpiredBetween(any(), any()) } returns 0
        every { subscriptionApi.countActiveSubscriptionsAsOf(any()) } returns 0
        every {
            repository.findByMetricTypeAndSnapshotDateAndLocationIdIsNull(MetricType.CHURN_RATE, any())
        } returns emptyList()
        every { repository.save(any()) } answers { firstArg() }

        scheduler.aggregateChurnRate(firstOfMonth)

        verify {
            repository.save(match<MetricsSnapshot> {
                it.metricType == MetricType.CHURN_RATE &&
                    it.value.compareTo(BigDecimal.ZERO) == 0
            })
        }
    }
}
