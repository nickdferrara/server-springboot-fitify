package com.nickdferrara.fitify.notification.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "device_tokens")
internal class DeviceToken(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "fcm_token")
    val fcmToken: String,

    @Column(name = "device_type")
    val deviceType: String,

    @Column(name = "last_used_at")
    var lastUsedAt: Instant? = null,
)
