package com.routehub.pos.models.responses

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)