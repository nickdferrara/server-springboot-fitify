package com.nickdferrara.fitify.logging.internal.config

import com.nickdferrara.fitify.logging.internal.aspect.AuditAspect
import com.nickdferrara.fitify.logging.internal.aspect.ExceptionLoggingAspect
import com.nickdferrara.fitify.logging.internal.aspect.LoggingAspect
import com.nickdferrara.fitify.logging.internal.service.AuditLogService
import com.nickdferrara.fitify.logging.internal.service.SensitiveDataMasker
import org.slf4j.MDC
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.task.TaskDecorator

@Configuration
@EnableConfigurationProperties(LoggingProperties::class)
internal class LoggingConfig {

    @Bean
    fun correlationIdFilter(properties: LoggingProperties): FilterRegistrationBean<CorrelationIdFilter> {
        val registration = FilterRegistrationBean(CorrelationIdFilter(properties))
        registration.order = Ordered.HIGHEST_PRECEDENCE
        return registration
    }

    @Bean
    fun mdcTaskExecutorCustomizer(): ThreadPoolTaskExecutorCustomizer {
        return ThreadPoolTaskExecutorCustomizer { executor ->
            executor.setTaskDecorator(MdcTaskDecorator())
        }
    }

    @Bean
    fun loggingAspect(properties: LoggingProperties, masker: SensitiveDataMasker): LoggingAspect {
        return LoggingAspect(properties, masker)
    }

    @Bean
    fun exceptionLoggingAspect(): ExceptionLoggingAspect {
        return ExceptionLoggingAspect()
    }

    @Bean
    fun auditAspect(auditLogService: AuditLogService, properties: LoggingProperties): AuditAspect {
        return AuditAspect(auditLogService, properties)
    }
}

internal class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
        return Runnable {
            try {
                MDC.setContextMap(contextMap)
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
    }
}
