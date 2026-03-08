package com.routehub.pos.models

data class PropertyType(
    val applyTo: List<String>,
    val _id: String,
    val typeName: String,
    val projectId: String,
    val module: List<String>
)