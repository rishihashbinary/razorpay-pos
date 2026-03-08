package com.routehub.pos.models

data class Property(
    val flatCount: Int,
    val status: Int,
    val units: Int,
    val _id: String,
    val qrCode: String,
    val zoneId: Zone,
    val wardId: Ward,
    val propertyTypeId: PropertyType,
    val propertyCategoryId: PropertyCategory,
    val propertyUsageTypeId: PropertyUsageType,
    val address1: String,
    val projectId: String,
    val userId: String,
    val phoneVerificationToken: String,
    val mobileNumbers: List<String>,
    val renters: List<String>,
    val additionalData: List<String>,
    val requestedChanges: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val latitude: Double,
    val longitude: Double,
    val consumerId: String
)