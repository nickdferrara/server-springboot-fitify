package com.nickdferrara.fitify.identity.internal.service.interfaces

import com.nickdferrara.fitify.identity.internal.dtos.request.ForgotPasswordRequest
import com.nickdferrara.fitify.identity.internal.dtos.request.RegisterRequest
import com.nickdferrara.fitify.identity.internal.dtos.request.ResetPasswordRequest
import com.nickdferrara.fitify.identity.internal.dtos.response.MessageResponse
import com.nickdferrara.fitify.identity.internal.dtos.response.RegisterResponse

internal interface AuthService {
    fun register(request: RegisterRequest): RegisterResponse
    fun forgotPassword(request: ForgotPasswordRequest): MessageResponse
    fun resetPassword(request: ResetPasswordRequest): MessageResponse
}
