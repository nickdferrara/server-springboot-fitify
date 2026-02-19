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

    private data class MigrationTarget(val table: String, val column: String)

    private val targets = listOf(
        MigrationTarget("users", "email"),
        MigrationTarget("locations", "email"),
    )

    override fun run(args: ApplicationArguments) {
        log.info("Starting ECB → SIV encryption migration")
        for (target in targets) {
            migrateTable(target.table, target.column)
        }
        log.info("ECB → SIV encryption migration complete")
    }

    private fun migrateTable(table: String, column: String) {
        val rows = jdbcTemplate.queryForList(
            "SELECT id, $column FROM $table WHERE $column IS NOT NULL AND $column != ''",
        )

        var migrated = 0
        var skipped = 0

        for (row in rows) {
            val id = row["id"] as UUID
            val ciphertext = row[column] as String

            if (encryptor.isSivEncrypted(ciphertext)) {
                skipped++
                continue
            }

            val plaintext = encryptor.decrypt(ciphertext)
            val reEncrypted = encryptor.encryptDeterministic(plaintext)

            jdbcTemplate.update(
                "UPDATE $table SET $column = ? WHERE id = ?",
                reEncrypted,
                id,
            )
            migrated++
        }

        log.info("Table '{}' column '{}': migrated={}, skipped={}", table, column, migrated, skipped)
    }
}
