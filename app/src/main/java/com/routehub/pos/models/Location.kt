package com.routehub.pos.models

data class Location(
    val type: String,
    val coordinates: List<Double>
)