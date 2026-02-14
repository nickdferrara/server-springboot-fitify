package com.nickdferrara.fitify.admin.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "business_rules",
    uniqueConstraints = [UniqueConstraint(columnNames = ["rule_key", "location_id"])],
)
internal class BusinessRule(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "rule_key", nullable = false)
    val ruleKey: String,

    @Column(nullable = false)
    var value: String,

    @Column(name = "location_id")
    val locationId: UUID? = null,

    var description: String? = null,

    @Column(name = "updated_by", nullable = false)
    var updatedBy: String,

    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: Instant? = null,
)
