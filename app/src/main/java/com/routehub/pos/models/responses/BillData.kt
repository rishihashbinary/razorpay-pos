package com.routehub.pos.models.responses

data class BillData(
    val bills: List<Bill>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)