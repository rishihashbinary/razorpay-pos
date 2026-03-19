package com.routehub.pos.models.responses

import com.routehub.pos.models.UserData

data class LoginResponse(
    val code: Int,
    val message: String,
    val data: UserData
)