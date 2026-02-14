package com.nickdferrara.fitify.subscription.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "subscription_plans")
internal class SubscriptionPlan(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    var planType: PlanType,

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    var basePrice: BigDecimal,

    @Column(name = "stripe_price_id", nullable = false)
    var stripePriceId: String,

    var active: Boolean = true,
)
