package com.nickdferrara.fitify.identity.internal.entities

import com.nickdferrara.fitify.shared.crypto.DeterministicEncryptedStringConverter
import com.nickdferrara.fitify.shared.crypto.NonDeterministicEncryptedStringConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
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
@Table(name = "users")
internal class User(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "keycloak_id", unique = true, nullable = false)
    val keycloakId: String,

    @Convert(converter = DeterministicEncryptedStringConverter::class)
    @Column(unique = true, nullable = false)
    var email: String,

    @Convert(converter = NonDeterministicEncryptedStringConverter::class)
    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Convert(converter = NonDeterministicEncryptedStringConverter::class)
    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "theme_preference", nullable = false)
    var themePreference: ThemePreference = ThemePreference.SYSTEM,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,
)
