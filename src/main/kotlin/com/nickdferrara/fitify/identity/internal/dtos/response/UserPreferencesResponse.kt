package com.nickdferrara.fitify.identity.internal.dtos.response

import com.nickdferrara.fitify.identity.internal.entities.User

internal data class UserPreferencesResponse(
    val theme: String,
)

internal fun User.toPreferencesResponse() = UserPreferencesResponse(
    theme = themePreference.name,
)
