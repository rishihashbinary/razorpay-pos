package com.routehub.pos.models.responses

data class Bill(
    val _id: String,
    val billPeriod: BillPeriod,
    val generationDate: String,
    val billAmount: Double,
    val outstanding: Double,
    val totalDue: Double,
    val status: Int
)