package com.nickdferrara.fitify.admin.internal.controller

import com.nickdferrara.fitify.admin.internal.dtos.response.MetricResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.OverviewResponse
import com.nickdferrara.fitify.admin.internal.entities.Granularity
import com.nickdferrara.fitify.admin.internal.service.MetricsService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/metrics")
@PreAuthorize("hasRole('ADMIN')")
internal class AdminMetricsController(
    private val metricsService: MetricsService,
) {

    @GetMapping("/signups")
    fun getSignups(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(defaultValue = "DAILY") granularity: Granularity,
        @RequestParam(required = false) locationId: UUID?,
    ): ResponseEntity<MetricResponse> {
        return ResponseEntity.ok(metricsService.getSignups(from, to, granularity, locationId))
    }

    @GetMapping("/cancellations")
    fun getCancellations(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(defaultValue = "DAILY") granularity: Granularity,
        @RequestParam(required = false) locationId: UUID?,
    ): ResponseEntity<MetricResponse> {
        return ResponseEntity.ok(metricsService.getCancellations(from, to, granularity, locationId))
    }

    @GetMapping("/revenue")
    fun getRevenue(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
        @RequestParam(defaultValue = "DAILY") granularity: Granularity,
        @RequestParam(required = false) locationId: UUID?,
    ): ResponseEntity<MetricResponse> {
        return ResponseEntity.ok(metricsService.getRevenue(from, to, granularity, locationId))
    }

    @GetMapping("/overview")
    fun getOverview(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        @RequestParam(required = false) locationId: UUID?,
    ): ResponseEntity<OverviewResponse> {
        val effectiveTo = to ?: LocalDate.now()
        val effectiveFrom = from ?: effectiveTo.minusDays(30)
        return ResponseEntity.ok(metricsService.getOverview(effectiveFrom, effectiveTo, locationId))
    }
}

@RestController
@RequestMapping("/api/v1/admin/locations/{locationId}/metrics")
@PreAuthorize("hasRole('ADMIN')")
internal class AdminLocationMetricsController(
    private val metricsService: MetricsService,
) {

    @GetMapping("/overview")
    fun getLocationOverview(
        @PathVariable locationId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
    ): ResponseEntity<OverviewResponse> {
        val effectiveTo = to ?: LocalDate.now()
        val effectiveFrom = from ?: effectiveTo.minusDays(30)
        return ResponseEntity.ok(metricsService.getOverview(effectiveFrom, effectiveTo, locationId))
    }
}
