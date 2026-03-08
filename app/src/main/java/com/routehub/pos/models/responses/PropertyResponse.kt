package com.routehub.pos.models.responses

import com.routehub.pos.models.Property

data class PropertyResponse(
    val code: Int,
    val message: String,
    val data: PropertyPage
)