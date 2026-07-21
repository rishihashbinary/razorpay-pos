package com.routehub.pos.models

data class Complaint(
    val _id: String? = null,
    val complaintNumber: String? = null,
    val title: String? = null,
    val description: String? = null,
    val complaintStatus: String? = null,
    val raisedDate: String? = null,
    val ageInDays: Int? = null
)