package com.nickdferrara.fitify.location.internal.entities

import com.nickdferrara.fitify.shared.crypto.DeterministicEncryptedStringConverter
import com.nickdferrara.fitify.shared.crypto.NonDeterministicEncryptedStringConverter
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "locations")
internal class Location(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    var name: String,

    var address: String,

    var city: String,

    var state: String,

    @Column(name = "zip_code")
    var zipCode: String,

    @Convert(converter = NonDeterministicEncryptedStringConverter::class)
    var phone: String,

    @Convert(converter = DeterministicEncryptedStringConverter::class)
    var email: String,

    @Column(name = "time_zone")
    var timeZone: String,

    var active: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: Instant? = null,

    @OneToMany(mappedBy = "location", cascade = [CascadeType.ALL], orphanRemoval = true)
    val operatingHours: MutableList<LocationOperatingHours> = mutableListOf(),
)
