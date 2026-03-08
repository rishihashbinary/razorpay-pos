package com.routehub.pos.models

data class PropertyCategory(
    val applyTo: List<String>,
    val _id: String,
    val categoryName: String,
    val propertyTypeId: String,
    val projectId: String,
    val module: List<String>,
    val status: Int
)