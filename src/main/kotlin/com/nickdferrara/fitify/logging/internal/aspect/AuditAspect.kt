package com.nickdferrara.fitify.logging.internal.aspect

import com.nickdferrara.fitify.logging.Audit
import com.nickdferrara.fitify.logging.internal.config.LoggingProperties
import com.nickdferrara.fitify.logging.internal.service.interfaces.AuditLogService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.MDC
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.UUID

@Aspect
internal class AuditAspect(
    private val auditLogService: AuditLogService,
    private val properties: LoggingProperties,
) {

    @Around("@annotation(audit)")
    fun auditMethod(joinPoint: ProceedingJoinPoint, audit: Audit): Any? {
        if (!properties.audit.enabled) return joinPoint.proceed()

        val result = joinPoint.proceed()

        val userId = MDC.get("userId") ?: "anonymous"
        val module = MDC.get("module") ?: "unknown"
        val resourceId = extractResourceId(joinPoint.args)
        val ipAddress = extractIpAddress()

        val details = if (audit.includeResult && result != null) {
            result.toString()
        } else {
            null
        }

        auditLogService.save(
            userId = userId,
            action = audit.action,
            module = module,
            resourceType = audit.resourceType,
            resourceId = resourceId,
            details = details,
            ipAddress = ipAddress,
        )

        return result
    }

    private fun extractResourceId(args: Array<Any?>): String? {
        return args.firstNotNullOfOrNull { arg ->
            when (arg) {
                is UUID -> arg.toString()
                is Long -> arg.toString()
                is String -> if (arg.length <= 64) arg else null
                else -> null
            }
        }
    }

    private fun extractIpAddress(): String? {
        return try {
            val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
                ?.request
            request?.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
                ?: request?.remoteAddr
        } catch (_: Exception) {
            null
        }
    }
}
