package com.routehub.pos.models

data class ProjectAccess(
    val status: Int,
    val roleIds: List<Role>,
    val _id: String,
    val userId: String,
    val projectId: Project,
    val grants: List<Grant>
)