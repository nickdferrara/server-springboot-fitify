package com.nickdferrara.fitify.notification.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notification_log")
internal class NotificationLog(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id")
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    val channel: NotificationChannel,

    @Column(name = "event_type")
    val eventType: String,

    @Column(columnDefinition = "jsonb")
    val payload: String,

    @Enumerated(EnumType.STRING)
    var status: NotificationStatus,

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false)
    val sentAt: Instant? = null,
)
