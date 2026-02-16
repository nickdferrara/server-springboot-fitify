package com.nickdferrara.fitify.admin.internal.exceptions

internal class BusinessRuleNotFoundException(ruleKey: String) :
    RuntimeException("Business rule not found: $ruleKey")
