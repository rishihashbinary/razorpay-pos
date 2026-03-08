package com.routehub.pos.models.responses

import com.routehub.pos.models.Property

data class PropertyPage(
    val page: String,
    val hitsPerPage: Int,
    val totalDataCount: Int,
    val totalPages: Int,
    val data: List<Property>
)