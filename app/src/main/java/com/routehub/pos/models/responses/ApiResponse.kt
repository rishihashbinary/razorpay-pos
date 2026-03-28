package com.routehub.pos.models.responses

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: List<T>?,
)