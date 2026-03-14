package com.routehub.pos.services

import com.routehub.pos.models.responses.PropertyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BillService {
    @GET("properties/details/page/1")
    fun updatePaymentStatus(
        @Query("qrCode") qrCode: String?
    ): Call<PropertyResponse>
}