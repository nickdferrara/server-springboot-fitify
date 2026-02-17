package com.nickdferrara.fitify.admin.internal.service

import com.nickdferrara.fitify.admin.internal.entities.enums.Granularity
import com.nickdferrara.fitify.admin.internal.entities.enums.MetricType
import com.nickdferrara.fitify.admin.internal.entities.MetricsSnapshot
import com.nickdferrara.fitify.admin.internal.exceptions.InvalidMetricsQueryException
import com.nickdferrara.fitify.admin.internal.repository.MetricsSnapshotRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class MetricsServiceTest {

    private val repository = mockk<MetricsSnapshotRepository>()
    private val service = MetricsServiceImpl(repository)

    private fun buildSnapshot(
        metricType: MetricType = MetricType.SIGNUPS,
        date: LocalDate = LocalDate.of(2025, 6, 1),
        value: BigDecimal = BigDecimal.ONE,
        locationId: UUID? = null,
        dimensions: Map<String, String> = emptyMap(),
    ) = MetricsSnapshot(
        id = UUID.randomUUID(),
        metricType = metricType,
        locationId = locationId,
        dimensions = dimensions,
        value = value,
        snapshotDate = date,
    )

    @Test
    fun `getSignups returns daily data points`() {
        val from = LocalDate.of(2025, 6, 1)
        val to = LocalDate.of(2025, 6, 3)
        val snapshots = listOf(
            buildSnapshot(date = from, value = BigDecimal(5)),
            buildSnapshot(date = from.plusDays(1), value = BigDecimal(3)),
            buildSnapshot(date = to, value = BigDecimal(7)),
        )

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(MetricType.SIGNUPS, any(), any())
        } returns snapshots

        val result = service.getSignups(from, to, Granularity.DAILY, null)

        assertEquals(3, result.dataPoints.size)
        assertEquals(MetricType.SIGNUPS, result.metricType)
        assertEquals(Granularity.DAILY, result.granularity)
    }

    @Test
    fun `getSignups rolls up to weekly`() {
        val from = LocalDate.of(2025, 6, 2) // Monday
        val to = LocalDate.of(2025, 6, 15)  // Sunday of week 2
        val snapshots = (0L..13L).map { offset ->
            buildSnapshot(date = from.plusDays(offset), value = BigDecimal.ONE)
        }

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(MetricType.SIGNUPS, any(), any())
        } returns snapshots

        val result = service.getSignups(from, to, Granularity.WEEKLY, null)

        assertEquals(2, result.dataPoints.size)
        assertEquals(BigDecimal(7), result.dataPoints[0].value)
        assertEquals(BigDecimal(7), result.dataPoints[1].value)
    }

    @Test
    fun `getSignups rolls up to monthly`() {
        val from = LocalDate.of(2025, 5, 1)
        val to = LocalDate.of(2025, 6, 30)
        val maySnapshots = (0L..30L).map { offset ->
            buildSnapshot(date = from.plusDays(offset), value = BigDecimal.ONE)
        }
        val juneSnapshots = (0L..29L).map { offset ->
            buildSnapshot(date = LocalDate.of(2025, 6, 1).plusDays(offset), value = BigDecimal(2))
        }
        val allSnapshots = maySnapshots + juneSnapshots

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(MetricType.SIGNUPS, any(), any())
        } returns allSnapshots

        val result = service.getSignups(from, to, Granularity.MONTHLY, null)

        assertEquals(2, result.dataPoints.size)
    }

    @Test
    fun `getMetric uses latest value for point-in-time metrics`() {
        val from = LocalDate.of(2025, 6, 1)
        val to = LocalDate.of(2025, 6, 7)
        val snapshots = listOf(
            buildSnapshot(metricType = MetricType.ACTIVE_SUBSCRIPTIONS, date = from, value = BigDecimal(100)),
            buildSnapshot(metricType = MetricType.ACTIVE_SUBSCRIPTIONS, date = from.plusDays(3), value = BigDecimal(105)),
            buildSnapshot(metricType = MetricType.ACTIVE_SUBSCRIPTIONS, date = to, value = BigDecimal(110)),
        )

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(MetricType.ACTIVE_SUBSCRIPTIONS, any(), any())
        } returns snapshots
        // Other metric types return empty for overview
        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(neq(MetricType.ACTIVE_SUBSCRIPTIONS), any(), any())
        } returns emptyList()

        val result = service.getOverview(from, to, null)
        val activeSubs = result.metrics.find { it.metricType == MetricType.ACTIVE_SUBSCRIPTIONS }

        assertNotNull(activeSubs)
        assertEquals(BigDecimal(110), activeSubs!!.currentValue)
    }

    @Test
    fun `trend comparison calculates change correctly`() {
        val from = LocalDate.of(2025, 6, 4)
        val to = LocalDate.of(2025, 6, 6)
        val currentSnapshots = listOf(
            buildSnapshot(date = from, value = BigDecimal(10)),
            buildSnapshot(date = from.plusDays(1), value = BigDecimal(15)),
            buildSnapshot(date = to, value = BigDecimal(5)),
        )
        val previousSnapshots = listOf(
            buildSnapshot(date = from.minusDays(3), value = BigDecimal(8)),
            buildSnapshot(date = from.minusDays(2), value = BigDecimal(12)),
            buildSnapshot(date = from.minusDays(1), value = BigDecimal(10)),
        )

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(MetricType.SIGNUPS, from, to)
        } returns currentSnapshots
        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(MetricType.SIGNUPS, from.minusDays(3), from.minusDays(1))
        } returns previousSnapshots

        val result = service.getSignups(from, to, Granularity.DAILY, null)

        assertNotNull(result.trend)
        assertEquals(BigDecimal(30), result.trend!!.currentPeriodTotal)
        assertEquals(BigDecimal(30), result.trend!!.previousPeriodTotal)
        assertEquals(0, result.trend!!.changeAbsolute.compareTo(BigDecimal.ZERO))
    }

    @Test
    fun `getSignups returns empty data points when no snapshots exist`() {
        val from = LocalDate.of(2025, 6, 1)
        val to = LocalDate.of(2025, 6, 7)

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(MetricType.SIGNUPS, any(), any())
        } returns emptyList()

        val result = service.getSignups(from, to, Granularity.DAILY, null)

        assertTrue(result.dataPoints.isEmpty())
    }

    @Test
    fun `getSignups uses location-specific queries when locationId provided`() {
        val from = LocalDate.of(2025, 6, 1)
        val to = LocalDate.of(2025, 6, 3)
        val locationId = UUID.randomUUID()
        val snapshots = listOf(
            buildSnapshot(date = from, value = BigDecimal(2), locationId = locationId),
        )

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationId(MetricType.SIGNUPS, any(), any(), locationId)
        } returns snapshots

        val result = service.getSignups(from, to, Granularity.DAILY, locationId)

        assertEquals(1, result.dataPoints.size)
    }

    @Test
    fun `getSignups throws when from is after to`() {
        assertThrows<InvalidMetricsQueryException> {
            service.getSignups(LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 1), Granularity.DAILY, null)
        }
    }

    @Test
    fun `trend changePercent is null when previous period total is zero`() {
        val from = LocalDate.of(2025, 6, 1)
        val to = LocalDate.of(2025, 6, 3)
        val currentSnapshots = listOf(
            buildSnapshot(date = from, value = BigDecimal(10)),
        )

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(MetricType.SIGNUPS, from, to)
        } returns currentSnapshots
        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(
                MetricType.SIGNUPS,
                from.minusDays(3),
                from.minusDays(1),
            )
        } returns emptyList()

        val result = service.getSignups(from, to, Granularity.DAILY, null)

        assertNotNull(result.trend)
        assertNull(result.trend!!.changePercent)
    }

    @Test
    fun `overview returns all metric types`() {
        val from = LocalDate.of(2025, 6, 1)
        val to = LocalDate.of(2025, 6, 7)

        every {
            repository.findByMetricTypeAndSnapshotDateBetweenAndLocationIdIsNull(any(), any(), any())
        } returns emptyList()

        val result = service.getOverview(from, to, null)

        assertEquals(7, result.metrics.size)
        assertEquals(from, result.from)
        assertEquals(to, result.to)
        assertNull(result.locationId)
    }
}
