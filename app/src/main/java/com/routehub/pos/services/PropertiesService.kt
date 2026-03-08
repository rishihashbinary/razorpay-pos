package com.routehub.pos.services

import com.routehub.pos.models.responses.PropertyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface PropertiesService {

    @GET("properties/{qr}")
    fun getProperty(
        @Path("qr") qrCode: String
    ): Call<PropertyResponse>

}