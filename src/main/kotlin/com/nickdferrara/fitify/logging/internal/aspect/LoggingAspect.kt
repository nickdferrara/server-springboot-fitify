package com.nickdferrara.fitify.logging.internal.aspect

import com.nickdferrara.fitify.logging.LogExecution
import com.nickdferrara.fitify.logging.LogLevel
import com.nickdferrara.fitify.logging.internal.config.LoggingProperties
import com.nickdferrara.fitify.logging.internal.util.SensitiveDataMasker
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Aspect
internal class LoggingAspect(
    private val properties: LoggingProperties,
    private val sensitiveDataMasker: SensitiveDataMasker,
) {

    @Around("@annotation(logExecution)")
    fun logAnnotatedMethod(joinPoint: ProceedingJoinPoint, logExecution: LogExecution): Any? {
        val logger = LoggerFactory.getLogger(joinPoint.target::class.java)
        return executeWithLogging(
            joinPoint = joinPoint,
            logger = logger,
            level = logExecution.level,
            includeArgs = logExecution.includeArgs,
            includeResult = logExecution.includeResult,
            timed = logExecution.timed,
        )
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *) && execution(public * *(..))")
    fun logControllerMethod(joinPoint: ProceedingJoinPoint): Any? {
        if (!properties.aop.controllerLogging) return joinPoint.proceed()
        if (hasLogExecutionAnnotation(joinPoint)) return joinPoint.proceed()

        val logger = LoggerFactory.getLogger(joinPoint.target::class.java)
        return executeWithLogging(
            joinPoint = joinPoint,
            logger = logger,
            level = LogLevel.INFO,
            includeArgs = true,
            includeResult = false,
            timed = true,
        )
    }

    @Around("within(@org.springframework.stereotype.Service *) && execution(public * *(..))")
    fun logServiceMethod(joinPoint: ProceedingJoinPoint): Any? {
        if (!properties.aop.serviceLogging) return joinPoint.proceed()
        if (hasLogExecutionAnnotation(joinPoint)) return joinPoint.proceed()

        val logger = LoggerFactory.getLogger(joinPoint.target::class.java)
        return executeWithLogging(
            joinPoint = joinPoint,
            logger = logger,
            level = LogLevel.DEBUG,
            includeArgs = true,
            includeResult = false,
            timed = true,
        )
    }

    private fun executeWithLogging(
        joinPoint: ProceedingJoinPoint,
        logger: Logger,
        level: LogLevel,
        includeArgs: Boolean,
        includeResult: Boolean,
        timed: Boolean,
    ): Any? {
        val methodName = joinPoint.signature.name
        val className = joinPoint.target::class.simpleName

        val argsString = if (includeArgs && joinPoint.args.isNotEmpty()) {
            if (properties.aop.maskSensitiveFields) {
                sensitiveDataMasker.maskArgs(joinPoint.args)
            } else {
                joinPoint.args.map { it?.toString() ?: "null" }
            }.joinToString(", ")
        } else {
            ""
        }

        log(logger, level, "{}.{} called{}", className, methodName,
            if (argsString.isNotEmpty()) " with args: [$argsString]" else "")

        val startTime = if (timed) System.currentTimeMillis() else 0L
        val result = joinPoint.proceed()
        val duration = if (timed) System.currentTimeMillis() - startTime else 0L

        val resultString = if (includeResult && result != null) " returned: $result" else ""
        val timeString = if (timed) " [${duration}ms]" else ""

        log(logger, level, "{}.{} completed{}{}",
            className, methodName, timeString, resultString)

        return result
    }

    private fun log(logger: Logger, level: LogLevel, message: String, vararg args: Any?) {
        when (level) {
            LogLevel.TRACE -> logger.trace(message, *args)
            LogLevel.DEBUG -> logger.debug(message, *args)
            LogLevel.INFO -> logger.info(message, *args)
            LogLevel.WARN -> logger.warn(message, *args)
            LogLevel.ERROR -> logger.error(message, *args)
        }
    }

    private fun hasLogExecutionAnnotation(joinPoint: ProceedingJoinPoint): Boolean {
        val method = (joinPoint.signature as MethodSignature).method
        return method.isAnnotationPresent(LogExecution::class.java)
    }
}
