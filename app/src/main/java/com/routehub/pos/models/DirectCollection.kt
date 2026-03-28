package com.routehub.pos.models

data class DirectCollection(
    val propertyId: String?,
    val amountPaid: Float?,
    val billAmount: Float?,
    val paymentType: String? =  null,
    val collectorId: String?,
    val collectionPeriod: CollectionPeriod,
    val remark: String?,
    val location: PropertyLocation?,
    val paymentStatus: String = "pending",
    val denialReason: String? = null,
)

data class CollectionPeriod(
    val month: Int,
    val year: Int
)

data class PropertyLocation(
    val latitude: Double,
    val longitude: Double
)
