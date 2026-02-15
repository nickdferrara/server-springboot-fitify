package com.nickdferrara.fitify.logging.internal.aspect

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.slf4j.MDC

@Aspect
internal class ExceptionLoggingAspect {

    @AfterThrowing(
        pointcut = "within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Service *)",
        throwing = "ex",
    )
    fun logException(joinPoint: JoinPoint, ex: Throwable) {
        val logger = LoggerFactory.getLogger(joinPoint.target::class.java)
        val methodName = joinPoint.signature.name
        val className = joinPoint.target::class.simpleName
        val correlationId = MDC.get("correlationId") ?: "unknown"
        val userId = MDC.get("userId") ?: "anonymous"

        if (isBusinessException(ex)) {
            logger.warn(
                "Business exception in {}.{} [correlationId={}, userId={}]: {}",
                className, methodName, correlationId, userId, ex.message,
            )
        } else {
            logger.error(
                "Exception in {}.{} [correlationId={}, userId={}]: {}",
                className, methodName, correlationId, userId, ex.message, ex,
            )
        }
    }

    private fun isBusinessException(ex: Throwable): Boolean {
        val exceptionName = ex::class.simpleName ?: return false
        return exceptionName.contains("NotFound") ||
            exceptionName.contains("Validation") ||
            exceptionName.contains("Conflict") ||
            exceptionName.contains("Unauthorized") ||
            exceptionName.contains("IllegalArgument") ||
            ex is IllegalArgumentException ||
            ex is IllegalStateException ||
            ex is NoSuchElementException
    }
}
