package com.nickdferrara.fitify.shared.crypto

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty("fitify.encryption.run-migration", havingValue = "true")
class EncryptionMigrationRunner(
    private val jdbcTemplate: JdbcTemplate,
    private val encryptor: AesEncryptor,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(EncryptionMigrationRunner::class.java)

    private enum class EncryptionMode { DETERMINISTIC, NON_DETERMINISTIC }

    private data class MigrationTarget(val table: String, val column: String, val mode: EncryptionMode)

    private val targets = listOf(
        MigrationTarget("users", "email", EncryptionMode.DETERMINISTIC),
        MigrationTarget("users", "first_name", EncryptionMode.NON_DETERMINISTIC),
        MigrationTarget("users", "last_name", EncryptionMode.NON_DETERMINISTIC),
        MigrationTarget("locations", "email", EncryptionMode.DETERMINISTIC),
        MigrationTarget("locations", "phone", EncryptionMode.NON_DETERMINISTIC),
    )

    override fun run(args: ApplicationArguments) {
        log.info("Starting encryption migration to versioned format")
        for (target in targets) {
            migrateTable(target)
        }
        log.info("Encryption migration to versioned format complete")
    }

    private fun migrateTable(target: MigrationTarget) {
        val rows = jdbcTemplate.queryForList(
            "SELECT id, ${target.column} FROM ${target.table} WHERE ${target.column} IS NOT NULL AND ${target.column} != ''",
        )

        var migrated = 0
        var skipped = 0

        for (row in rows) {
            val id = row["id"] as UUID
            val ciphertext = row[target.column] as String

            if (encryptor.isVersioned(ciphertext)) {
                skipped++
                continue
            }

            val plaintext = encryptor.decrypt(ciphertext)
            val reEncrypted = when (target.mode) {
                EncryptionMode.DETERMINISTIC -> encryptor.encryptDeterministic(plaintext)
                EncryptionMode.NON_DETERMINISTIC -> encryptor.encryptNonDeterministic(plaintext)
            }

            jdbcTemplate.update(
                "UPDATE ${target.table} SET ${target.column} = ? WHERE id = ?",
                reEncrypted,
                id,
            )
            migrated++
        }

        log.info("Table '{}' column '{}': migrated={}, skipped={}", target.table, target.column, migrated, skipped)
    }
}
