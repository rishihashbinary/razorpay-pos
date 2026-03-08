package com.routehub.pos.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

class PropertiesService {

    @GET("properties/{qr}")
    fun getProperty(
        @Path("qr") qrCode: String
    ): Call<PropertyResponse>

}