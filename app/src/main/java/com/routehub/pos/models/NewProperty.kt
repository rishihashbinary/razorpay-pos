package com.routehub.pos.models

data class NewProperty(
    val address1: String,
    val lat: Double,
    val lon: Double,
    val mobileNo: String,
    val name: String,
    val propertyCategoryId: String?,
    val propertyTypeId: String?,
    val propertyUsageTypeId: String?,
    val qrCode: String,
    val wardId: String?,
    val zoneId: String?
)