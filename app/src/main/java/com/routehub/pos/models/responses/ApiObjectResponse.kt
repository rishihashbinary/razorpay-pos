package com.routehub.pos.models.responses

data class ApiObjectResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
)