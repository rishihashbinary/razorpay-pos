package com.routehub.pos.models.responses

import com.routehub.pos.models.Complaint

data class ComplaintsData(
    val data: List<Complaint> = emptyList()
)