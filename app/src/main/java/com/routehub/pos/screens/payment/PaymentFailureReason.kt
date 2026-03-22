package com.routehub.pos.screens.payment

enum class PaymentFailureReason(val displayName: String) {
    CUSTOMER_DECLINED("Customer Refused to Pay"),
    INSUFFICIENT_FUNDS("Insufficient Funds"),
    NETWORK_ISSUE("Network Issue"),
    TECHNICAL_ERROR("Technical Error"),
    WRONG_AMOUNT("Wrong Amount"),
    OTHER("Other")
}