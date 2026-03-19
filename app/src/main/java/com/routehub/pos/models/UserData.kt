package com.routehub.pos.models

data class UserData(
    val _id: String,
    val email: String,
    val contact: String,
    val loginName: String,
    val firstName: String,
    val lastName: String,
    val employeeId: String,
    val token: String,
    val projectAccessList: List<ProjectAccess>
)