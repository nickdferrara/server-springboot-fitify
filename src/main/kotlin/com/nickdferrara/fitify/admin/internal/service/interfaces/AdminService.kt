package com.nickdferrara.fitify.admin.internal.service.interfaces

import com.nickdferrara.fitify.admin.internal.dtos.request.CreateClassRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.CreateRecurringScheduleRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.UpdateBusinessRuleRequest
import com.nickdferrara.fitify.admin.internal.dtos.request.UpdateClassRequest
import com.nickdferrara.fitify.admin.internal.dtos.response.AdminClassResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.BusinessRuleResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.CancelClassResponse
import com.nickdferrara.fitify.admin.internal.dtos.response.RecurringScheduleResponse
import java.util.UUID

internal interface AdminService {
    fun createClass(locationId: UUID, request: CreateClassRequest): AdminClassResponse
    fun updateClass(classId: UUID, request: UpdateClassRequest): AdminClassResponse
    fun cancelClass(classId: UUID): CancelClassResponse
    fun listClassesByLocation(locationId: UUID): List<AdminClassResponse>
    fun createRecurringSchedule(locationId: UUID, request: CreateRecurringScheduleRequest): RecurringScheduleResponse
    fun listBusinessRules(): List<BusinessRuleResponse>
    fun updateBusinessRule(ruleKey: String, request: UpdateBusinessRuleRequest, updatedBy: String): BusinessRuleResponse
}
