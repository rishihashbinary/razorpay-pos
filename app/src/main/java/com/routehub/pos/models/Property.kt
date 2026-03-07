package com.routehub.pos.models

data class Property(

    val qrCode: String?,
    val name: String,
    val mobileNo: String,

    val propertyType: String,
    val propertyCategory: String,
    val usageType: String,

    val latitude: Double,
    val longitude: Double,

    val feeAmount: Int

)