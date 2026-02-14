package com.nickdferrara.fitify.subscription.internal.exception

internal class SubscriptionNotFoundException(message: String) : RuntimeException(message)

internal class SubscriptionPlanNotFoundException(message: String) : RuntimeException(message)

internal class ActiveSubscriptionExistsException(message: String) : RuntimeException(message)

internal class StripeException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

internal class InvalidWebhookSignatureException(message: String) : RuntimeException(message)

internal class SubscriptionStateException(message: String) : RuntimeException(message)
