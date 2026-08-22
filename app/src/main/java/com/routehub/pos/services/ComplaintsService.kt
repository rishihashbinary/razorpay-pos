package com.routehub.pos.services

import com.routehub.pos.models.Complaint
import com.routehub.pos.models.responses.ApiListResponse
import com.routehub.pos.models.responses.ApiObjectResponse
import com.routehub.pos.models.responses.ComplaintsData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ComplaintsService {
    @GET("complaints")
    fun getComplaints(@Query("propertyId") propertyId: String?, @Query("details") details: String?,@Query("isAdmin") isAdmin: String? ): Call<ApiListResponse<Complaint>>
}