package com.routehub.pos.services

import com.routehub.pos.models.responses.PropertyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PropertiesService {

    @GET("properties/details/page/1")
    fun getPropertyByQr(
        @Query("qrCode") qrCode: String?
    ): Call<PropertyResponse>

}