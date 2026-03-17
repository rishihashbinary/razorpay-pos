package com.routehub.pos.services

import com.routehub.pos.models.responses.PropertyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PropertiesService {

    @GET("properties/by-qr")
    fun getPropertyByQr(
        @Query("qrCode") qrCode: String?, @Query("includeRate") includeRate: String?
    ): Call<PropertyResponse>

    @GET("properties/by-mobile")
    fun getPropertyByMobileNumber(
        @Query("phone") phone: String?
    ): Call<PropertyResponse>

}