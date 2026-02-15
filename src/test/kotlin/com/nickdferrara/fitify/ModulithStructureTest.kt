package com.nickdferrara.fitify

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import kotlin.test.assertEquals

class ModulithStructureTest {

    private val modules = ApplicationModules.of(FitifyApplication::class.java)

    @Test
    fun verifyModuleStructure() {
        modules.verify()
    }

    @Test
    fun generateDocumentation() {
        Documenter(modules).writeDocumentation()
    }

    @Test
    fun shouldDetectAllModules() {
        @Suppress("DEPRECATION")
        val moduleNames = modules.map { it.name }.sorted()
        assertEquals(
            listOf("admin", "coaching", "identity", "location", "logging", "notification", "scheduling", "security", "shared", "subscription"),
            moduleNames,
        )
    }
}
