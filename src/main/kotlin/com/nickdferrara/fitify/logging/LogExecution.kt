package com.nickdferrara.fitify.logging

enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogExecution(
    val level: LogLevel = LogLevel.INFO,
    val includeArgs: Boolean = true,
    val includeResult: Boolean = false,
    val timed: Boolean = true,
)
