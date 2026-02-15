package com.nickdferrara.fitify.logging.internal.service

import com.nickdferrara.fitify.logging.Sensitive
import org.springframework.stereotype.Component

@Component
internal class SensitiveDataMasker {

    companion object {
        const val MASKED_VALUE = "***MASKED***"
        private val SENSITIVE_FIELD_NAMES = setOf(
            "password", "secret", "token", "apikey", "apiKey",
            "creditcard", "creditCard", "creditCardNumber",
            "ssn", "authorization",
        )
    }

    fun maskArgs(args: Array<Any?>): List<String> {
        return args.map { arg -> maskValue(arg) }
    }

    private fun maskValue(value: Any?): String {
        if (value == null) return "null"

        return try {
            val clazz = value::class
            val fields = clazz.java.declaredFields

            val hasSensitiveFields = fields.any { field ->
                isSensitiveFieldName(field.name) || field.isAnnotationPresent(Sensitive::class.java)
            }

            if (!hasSensitiveFields) return value.toString()

            val masked = fields.map { field ->
                field.isAccessible = true
                val fieldValue = field.get(value)
                if (isSensitiveFieldName(field.name) || field.isAnnotationPresent(Sensitive::class.java)) {
                    "${field.name}=$MASKED_VALUE"
                } else {
                    "${field.name}=$fieldValue"
                }
            }
            "${clazz.simpleName}(${masked.joinToString(", ")})"
        } catch (_: Exception) {
            value.toString()
        }
    }

    private fun isSensitiveFieldName(name: String): Boolean {
        return SENSITIVE_FIELD_NAMES.any { it.equals(name, ignoreCase = true) }
    }
}
