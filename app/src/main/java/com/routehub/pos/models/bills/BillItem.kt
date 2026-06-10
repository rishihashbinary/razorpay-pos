package com.routehub.pos.models.bills

data class BillItem(
    val id: String,
    val month: String,
    val amount: Double,
    val status: String, // "PAID" / "PENDING"
    val createdAt: String
)