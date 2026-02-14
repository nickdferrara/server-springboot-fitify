package com.nickdferrara.fitify.admin.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "recurring_schedules")
internal class RecurringSchedule(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "location_id", nullable = false)
    val locationId: UUID,

    @Column(nullable = false)
    var name: String,

    var description: String? = null,

    @Column(name = "class_type", nullable = false)
    var classType: String,

    @Column(name = "coach_id", nullable = false)
    var coachId: UUID,

    var room: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "days_of_week", columnDefinition = "jsonb", nullable = false)
    var daysOfWeek: List<String>,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime,

    @Column(name = "duration_minutes", nullable = false)
    var durationMinutes: Int,

    var capacity: Int,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate,

    var active: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,
)
