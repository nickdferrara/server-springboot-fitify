package com.nickdferrara.fitify.security.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "location_admin_assignments",
    uniqueConstraints = [UniqueConstraint(columnNames = ["keycloak_id", "location_id"])],
)
internal class LocationAdminAssignment(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "keycloak_id", nullable = false)
    val keycloakId: String,

    @Column(name = "location_id", nullable = false)
    val locationId: UUID,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,
)
