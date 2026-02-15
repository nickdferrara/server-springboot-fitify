package com.nickdferrara.fitify.logging.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audit_logs")
internal class AuditLogEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    val timestamp: Instant? = null,

    @Column(name = "user_id")
    val userId: String,

    val action: String,

    val module: String,

    @Column(name = "resource_type")
    val resourceType: String,

    @Column(name = "resource_id")
    val resourceId: String? = null,

    @Column(columnDefinition = "text")
    val details: String? = null,

    @Column(name = "ip_address")
    val ipAddress: String? = null,
)
