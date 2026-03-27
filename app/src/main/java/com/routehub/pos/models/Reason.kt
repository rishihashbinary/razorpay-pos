package com.routehub.pos.models

data class Reason(
    val status: Int,
    val _id: String,
    val reason: String,
    val type: String,
    val projectId: String,
    val createdAt: String,
    val updatedAt: String
)