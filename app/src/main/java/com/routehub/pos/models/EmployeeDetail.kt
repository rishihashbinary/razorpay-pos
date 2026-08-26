package com.routehub.pos.models

data class EmployeeWard(
    val _id: String,
    val number: String? = null
)

data class EmployeeZone(
    val _id: String,
    val name: String? = null
)

data class EmployeeDetail(
    val wardIds: List<EmployeeWard> = emptyList(),
    val zoneIds: List<EmployeeZone> = emptyList()
)