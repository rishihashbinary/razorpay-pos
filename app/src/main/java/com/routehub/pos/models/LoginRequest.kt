package com.routehub.pos.models

data class LoginRequest(
    val email: String,
    val password: String
)