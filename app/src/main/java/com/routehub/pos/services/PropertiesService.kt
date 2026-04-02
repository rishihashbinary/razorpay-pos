package com.routehub.pos.services

import com.routehub.pos.models.PropertyCategory
import com.routehub.pos.models.PropertyType
import com.routehub.pos.models.PropertyUsageType
import com.routehub.pos.models.responses.ApiResponse
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

    @GET("api/propertyType")
    suspend fun getPropertyTypes(): ApiResponse<PropertyType>


    @GET("api/propertyCategory")
    suspend fun getCategories(): ApiResponse<PropertyCategory>

    @GET("api/propertyUsageType")
    suspend fun getUsageTypes(): ApiResponse<PropertyUsageType>


}