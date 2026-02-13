package com.nickdferrara.fitify.coaching.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "coach_locations")
internal class CoachLocation(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    var coach: Coach? = null,

    @Column(name = "location_id", nullable = false)
    val locationId: UUID,

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    val assignedAt: Instant? = null,
)
