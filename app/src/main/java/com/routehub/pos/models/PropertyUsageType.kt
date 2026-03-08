package com.routehub.pos.models

data class PropertyUsageType(
    val applyTo: List<String>,
    val _id: String,
    val typeName: String,
    val propertyCategoryId: String,
    val projectId: String,
    val module: List<String>,
    val status: Int
)