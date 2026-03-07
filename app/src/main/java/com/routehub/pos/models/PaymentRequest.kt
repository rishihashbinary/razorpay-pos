package com.routehub.pos.models

data class PaymentRequest(

    val amount: Int,
    val orderId: String,
    val description: String,
    val customerPhone: String

)