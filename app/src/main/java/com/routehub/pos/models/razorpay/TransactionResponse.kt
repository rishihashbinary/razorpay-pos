package com.routehub.pos.models.razorpay

data class TransactionResponse(
    val status: String?,
    val error: String?,
    val result: Result?
)

data class Result(
    val txn: Txn?,
    val customer: Customer?,
    val receipt: Receipt?,
    val references: References?
)

data class Txn(
    val txnId: String?,
    val paymentMode: String?,
    val amount: String?,
    val status: String?,
    val txnType: String?
)

data class Customer(
    val email: String?,
    val mobileNo: String?,
    val name: String?
)

data class Receipt(
    val receiptDate: String?,
    val receiptUrl: String?
)

data class References(
    val reference1: String?
)