package com.nickdferrara.fitify.admin.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "metrics_snapshots")
internal class MetricsSnapshot(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    val metricType: MetricType,

    @Column(name = "location_id")
    val locationId: UUID? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var dimensions: Map<String, String> = emptyMap(),

    @Column(nullable = false, precision = 19, scale = 4)
    var value: BigDecimal = BigDecimal.ZERO,

    @Column(name = "snapshot_date", nullable = false)
    val snapshotDate: LocalDate,
)
