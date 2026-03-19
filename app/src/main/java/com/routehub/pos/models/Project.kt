package com.routehub.pos.models

data class Project(
    val _id: String,
    val name: String,
    val customerId: String,
    val kmlFileUrl: String,
    val warning: Boolean,
    val suspended: Boolean,
    val warningMessage: String,
    val modules: List<Any>,
    val location: Location
)