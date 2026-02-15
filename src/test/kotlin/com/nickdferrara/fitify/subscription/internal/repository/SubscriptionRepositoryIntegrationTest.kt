package com.nickdferrara.fitify.subscription.internal.repository

import com.nickdferrara.fitify.subscription.internal.entities.PlanType
import com.nickdferrara.fitify.subscription.internal.entities.Subscription
import com.nickdferrara.fitify.subscription.internal.entities.SubscriptionStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIf("isDockerAvailable")
internal class SubscriptionRepositoryIntegrationTest {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        fun isDockerAvailable(): Boolean {
            return try {
                val process = ProcessBuilder("docker", "info").start()
                process.waitFor() == 0
            } catch (_: Exception) {
                false
            }
        }
    }

    @Autowired
    lateinit var repository: SubscriptionRepository

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    private fun buildSubscription(
        userId: UUID = UUID.randomUUID(),
        planType: PlanType = PlanType.MONTHLY,
        status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
        periodEnd: Instant? = Instant.now().plus(30, ChronoUnit.DAYS),
    ) = Subscription(
        userId = userId,
        planType = planType,
        status = status,
        stripeSubscriptionId = "sub_${UUID.randomUUID().toString().take(8)}",
        currentPeriodStart = Instant.now(),
        currentPeriodEnd = periodEnd,
    )

    @Test
    fun `findByUserId returns all subscriptions for a user`() {
        val userId = UUID.randomUUID()
        repository.save(buildSubscription(userId = userId))
        repository.save(buildSubscription(userId = userId, planType = PlanType.ANNUAL))
        repository.save(buildSubscription())

        val results = repository.findByUserId(userId)

        assertThat(results).hasSize(2)
    }

    @Test
    fun `findByStripeSubscriptionId returns matching subscription`() {
        val sub = repository.save(buildSubscription())

        val result = repository.findByStripeSubscriptionId(sub.stripeSubscriptionId!!)

        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(sub.id)
    }

    @Test
    fun `findByUserIdAndStatusIn returns subscription with matching status`() {
        val userId = UUID.randomUUID()
        repository.save(buildSubscription(userId = userId, status = SubscriptionStatus.ACTIVE))
        repository.save(buildSubscription(userId = userId, status = SubscriptionStatus.EXPIRED))

        val result = repository.findByUserIdAndStatusIn(
            userId,
            listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.CANCELLING),
        )

        assertThat(result).isNotNull
        assertThat(result!!.status).isEqualTo(SubscriptionStatus.ACTIVE)
    }

    @Test
    fun `existsByUserIdAndStatusIn returns true when matching subscription exists`() {
        val userId = UUID.randomUUID()
        repository.save(buildSubscription(userId = userId, status = SubscriptionStatus.ACTIVE))

        val exists = repository.existsByUserIdAndStatusIn(userId, listOf(SubscriptionStatus.ACTIVE))

        assertThat(exists).isTrue()
    }

    @Test
    fun `countByStatusInGroupByPlanType returns counts grouped by plan type`() {
        repository.save(buildSubscription(planType = PlanType.MONTHLY, status = SubscriptionStatus.ACTIVE))
        repository.save(buildSubscription(planType = PlanType.MONTHLY, status = SubscriptionStatus.ACTIVE))
        repository.save(buildSubscription(planType = PlanType.ANNUAL, status = SubscriptionStatus.ACTIVE))

        val results = repository.countByStatusInGroupByPlanType(listOf(SubscriptionStatus.ACTIVE))

        assertThat(results).hasSize(2)
    }

    @Test
    fun `countExpiredBetween returns count of expired subscriptions in date range`() {
        val now = Instant.now()
        repository.save(
            buildSubscription(
                status = SubscriptionStatus.EXPIRED,
                periodEnd = now.minus(5, ChronoUnit.DAYS),
            )
        )
        repository.save(
            buildSubscription(
                status = SubscriptionStatus.EXPIRED,
                periodEnd = now.minus(15, ChronoUnit.DAYS),
            )
        )
        repository.save(buildSubscription(status = SubscriptionStatus.ACTIVE))

        val count = repository.countExpiredBetween(
            now.minus(10, ChronoUnit.DAYS),
            now,
        )

        assertThat(count).isEqualTo(1)
    }
}
