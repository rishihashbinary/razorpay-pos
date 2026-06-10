package com.routehub.pos.models.responses

data class ApiListResponse<T>(
    val success: Boolean,
    val message: String,
    val data: List<T>?,
)