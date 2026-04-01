package com.routehub.pos.models

data class ReceiptData(
    val merchantName: String? = "",
    val txnId: String? = "",
    val paymentMode: String? = "",
    val reference1: String? = "",
    val status: String? = "",
    val amount: Float? = 0F,
    val customerName: String? = "",
    val usageType: String? = "",
    val customerPhone: String? = "",
    val receiptDate: String? = ""
)