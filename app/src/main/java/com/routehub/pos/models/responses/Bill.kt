package com.routehub.pos.models.responses

data class Bill(
    val billPeriod: BillPeriod,
    val generationDate: String,
    val billAmount: Int,
    val outstanding: Int,
    val totalDue: Int,
    val status: Int
)