package com.nickdferrara.fitify.subscription.internal.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "discount_strategies")
internal class DiscountStrategy(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "strategy_type", nullable = false)
    var strategyType: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var value: BigDecimal,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var conditions: Map<String, Any> = emptyMap(),

    var priority: Int = 0,

    var active: Boolean = true,
)
