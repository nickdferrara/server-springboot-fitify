package com.nickdferrara.fitify.identity.internal.service.interfaces

import com.nickdferrara.fitify.identity.internal.dtos.request.UpdatePreferencesRequest
import com.nickdferrara.fitify.identity.internal.dtos.response.UserPreferencesResponse

internal interface IdentityService {
    fun getPreferences(keycloakId: String): UserPreferencesResponse
    fun updatePreferences(keycloakId: String, request: UpdatePreferencesRequest): UserPreferencesResponse
}
