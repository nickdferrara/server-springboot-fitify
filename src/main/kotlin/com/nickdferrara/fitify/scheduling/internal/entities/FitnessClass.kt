package com.nickdferrara.fitify.scheduling.internal.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "fitness_classes")
internal class FitnessClass(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "location_id")
    var locationId: UUID,

    var name: String,

    @Column(name = "class_type")
    var classType: String,

    @Column(name = "coach_id")
    var coachId: UUID,

    var room: String? = null,

    @Column(name = "start_time")
    var startTime: Instant,

    @Column(name = "end_time")
    var endTime: Instant,

    var capacity: Int,

    @Enumerated(EnumType.STRING)
    var status: FitnessClassStatus = FitnessClassStatus.ACTIVE,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @OneToMany(mappedBy = "fitnessClass", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bookings: MutableList<Booking> = mutableListOf(),

    @OneToMany(mappedBy = "fitnessClass", cascade = [CascadeType.ALL], orphanRemoval = true)
    val waitlistEntries: MutableList<WaitlistEntry> = mutableListOf(),
)
