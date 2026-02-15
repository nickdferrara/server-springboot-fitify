package com.nickdferrara.fitify.scheduling.internal.repository

import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClass
import com.nickdferrara.fitify.scheduling.internal.entities.FitnessClassStatus
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
internal class FitnessClassRepositoryIntegrationTest {

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
    lateinit var repository: FitnessClassRepository

    private val locationId: UUID = UUID.randomUUID()
    private val coachId: UUID = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    private fun buildClass(
        startTime: Instant = Instant.now().plus(2, ChronoUnit.DAYS),
        endTime: Instant = Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
        status: FitnessClassStatus = FitnessClassStatus.ACTIVE,
        coachId: UUID = this.coachId,
        locationId: UUID = this.locationId,
    ) = FitnessClass(
        locationId = locationId,
        name = "Test Class",
        classType = "yoga",
        coachId = coachId,
        room = "Room A",
        startTime = startTime,
        endTime = endTime,
        capacity = 20,
        status = status,
    )

    @Test
    fun `findByLocationIdAndStartTimeAfter returns future classes at location`() {
        val futureClass = repository.save(buildClass())
        val pastClass = repository.save(
            buildClass(
                startTime = Instant.now().minus(2, ChronoUnit.DAYS),
                endTime = Instant.now().minus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
            )
        )
        val otherLocation = repository.save(buildClass(locationId = UUID.randomUUID()))

        val results = repository.findByLocationIdAndStartTimeAfterOrderByStartTimeAsc(locationId, Instant.now())

        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo(futureClass.id)
    }

    @Test
    fun `findByCoachIdAndTimeRange returns active classes for coach in time range`() {
        val start = Instant.now().plus(1, ChronoUnit.DAYS)
        val end = start.plus(1, ChronoUnit.HOURS)
        val matchingClass = repository.save(buildClass(startTime = start, endTime = end))
        val cancelledClass = repository.save(
            buildClass(startTime = start, endTime = end, status = FitnessClassStatus.CANCELLED)
        )
        val outOfRange = repository.save(
            buildClass(
                startTime = Instant.now().plus(10, ChronoUnit.DAYS),
                endTime = Instant.now().plus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
            )
        )

        val results = repository.findByCoachIdAndTimeRange(
            coachId,
            start.minus(1, ChronoUnit.HOURS),
            end.plus(1, ChronoUnit.HOURS),
        )

        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo(matchingClass.id)
    }

    @Test
    fun `findWithBookingsByDateRange returns active classes in range`() {
        val start = Instant.now().plus(1, ChronoUnit.DAYS)
        val end = start.plus(1, ChronoUnit.HOURS)
        val inRange = repository.save(buildClass(startTime = start, endTime = end))
        val outOfRange = repository.save(
            buildClass(
                startTime = Instant.now().plus(10, ChronoUnit.DAYS),
                endTime = Instant.now().plus(10, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
            )
        )

        val results = repository.findWithBookingsByDateRange(
            start.minus(1, ChronoUnit.HOURS),
            start.plus(2, ChronoUnit.HOURS),
        )

        assertThat(results).hasSize(1)
        assertThat(results[0].id).isEqualTo(inRange.id)
    }

    @Test
    fun `findAll with specification filters by class type`() {
        repository.save(buildClass())
        val pilatesClass = repository.save(
            FitnessClass(
                locationId = locationId,
                name = "Pilates Class",
                classType = "pilates",
                coachId = coachId,
                room = "Room B",
                startTime = Instant.now().plus(3, ChronoUnit.DAYS),
                endTime = Instant.now().plus(3, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
                capacity = 15,
            )
        )

        val spec = FitnessClassSpecifications.hasClassType("pilates")
        val results = repository.findAll(spec)

        assertThat(results).hasSize(1)
        assertThat(results[0].classType).isEqualTo("pilates")
    }
}
