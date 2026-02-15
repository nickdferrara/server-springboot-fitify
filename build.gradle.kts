plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.jpa") version "2.1.20"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.nickdferrara"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["springModulithVersion"] = "1.3.10"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Modulith
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-events-api")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Keycloak
    implementation("org.keycloak:keycloak-admin-client:26.0.0")

    // Rate Limiting
    implementation("com.bucket4j:bucket4j-core:8.10.1")

    // Stripe
    implementation("com.stripe:stripe-java:28.2.0")

    // AOP
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // Notification
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.google.firebase:firebase-admin:9.4.3")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.github.dasniko:testcontainers-keycloak:3.5.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

val modulePackages = mapOf(
    "admin" to "com/nickdferrara/fitify/admin/**",
    "coaching" to "com/nickdferrara/fitify/coaching/**",
    "identity" to "com/nickdferrara/fitify/identity/**",
    "location" to "com/nickdferrara/fitify/location/**",
    "notification" to "com/nickdferrara/fitify/notification/**",
    "scheduling" to "com/nickdferrara/fitify/scheduling/**",
    "security" to "com/nickdferrara/fitify/security/**",
    "shared" to "com/nickdferrara/fitify/shared/**",
    "subscription" to "com/nickdferrara/fitify/subscription/**",
    "logging" to "com/nickdferrara/fitify/logging/**",
)

val commonExcludes = listOf(
    "**/*Dto*",
    "**/*Request*",
    "**/*Response*",
    "**/*Config*",
    "**/*Properties*",
    "**/*Advice*",
    "**/entities/**",
    "**/dtos/**",
    "**/*Aspect*",
    "**/*Filter*",
)

tasks.jacocoTestCoverageVerification {
    violationRules {
        modulePackages.forEach { (_, includePattern) ->
            rule {
                element = "BUNDLE"
                includes = listOf(includePattern)
                excludes = commonExcludes
                limit {
                    counter = "LINE"
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
