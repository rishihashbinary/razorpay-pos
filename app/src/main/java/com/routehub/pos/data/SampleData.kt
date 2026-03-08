package com.routehub.pos.data

import com.routehub.pos.models.Property
import com.routehub.pos.models.PropertyCategory
import com.routehub.pos.models.PropertyType
import com.routehub.pos.models.PropertyUsageType

object SampleData {

    val sampleProperty = Property(

        qrCode = "MUNI393555",

        name = "Amit",
        mobileNo = "8707180901",

        propertyTypeId = PropertyType(listOf("Property"), "123", "Residential", "123", listOf("Property")),
        propertyCategoryId = PropertyCategory(listOf("Property"), "123", "Household / Residential", "123", "123", listOf("Property"), 1),
        propertyUsageTypeId = PropertyUsageType(listOf("Property"), "123", "11 - 20 rooms", "123", "123", listOf("Property"), 1),

        latitude = 31.6557414,
        longitude = 74.8625776

    )

}