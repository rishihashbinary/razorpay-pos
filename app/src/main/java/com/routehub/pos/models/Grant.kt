package com.routehub.pos.models

data class Grant(
    val name: String,
    val action: String,
    val read: Boolean,
    val write: Boolean,
    val update: Boolean,
    val delete: Boolean
)