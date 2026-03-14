package com.routehub.pos.models

data class Payment(

    val amount: Int,
    val orderId: String,
    val description: String,
    val propertyId: String,

    // Response fields
    val status: String,
    val paymentId: String?,
    val method: String?,
    val rrn: String?,
    val errorMessage: String?


)