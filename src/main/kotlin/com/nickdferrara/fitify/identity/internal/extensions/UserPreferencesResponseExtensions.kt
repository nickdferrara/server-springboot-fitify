package com.nickdferrara.fitify.identity.internal.extensions

import com.nickdferrara.fitify.identity.internal.dtos.response.UserPreferencesResponse
import com.nickdferrara.fitify.identity.internal.entities.User

internal fun User.toPreferencesResponse() = UserPreferencesResponse(
    theme = themePreference.name,
)
