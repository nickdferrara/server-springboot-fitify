package com.nickdferrara.fitify.logging

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Audit(
    val action: String,
    val resourceType: String,
    val includeResult: Boolean = false,
)
