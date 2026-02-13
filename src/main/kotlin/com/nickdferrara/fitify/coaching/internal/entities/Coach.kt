package com.nickdferrara.fitify.coaching.internal.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "coaches")
internal class Coach(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    var name: String,

    var bio: String,

    @Column(name = "photo_url")
    var photoUrl: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var specializations: List<String> = emptyList(),

    var active: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @OneToMany(mappedBy = "coach", cascade = [CascadeType.ALL], orphanRemoval = true)
    val certifications: MutableList<CoachCertification> = mutableListOf(),

    @OneToMany(mappedBy = "coach", cascade = [CascadeType.ALL], orphanRemoval = true)
    val locations: MutableList<CoachLocation> = mutableListOf(),
)
