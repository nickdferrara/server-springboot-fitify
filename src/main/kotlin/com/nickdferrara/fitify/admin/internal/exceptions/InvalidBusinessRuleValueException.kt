package com.nickdferrara.fitify.admin.internal.exceptions

internal class InvalidBusinessRuleValueException(ruleKey: String, reason: String) :
    RuntimeException("Invalid value for business rule '$ruleKey': $reason")
