package com.routehub.pos.services

import com.routehub.pos.models.NewProperty
import com.routehub.pos.models.PropertyCategory
import com.routehub.pos.models.PropertyType
import com.routehub.pos.models.PropertyUsageType
import com.routehub.pos.models.responses.ApiListResponse
import com.routehub.pos.models.responses.ApiObjectResponse
import com.routehub.pos.models.responses.PropertyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PropertiesService {

    @GET("properties/by-qr")
    fun getPropertyByQr(
        @Query("qrCode") qrCode: String?, @Query("includeRate") includeRate: String?
    ): Call<PropertyResponse>

    @GET("properties/by-mobile")
    fun getPropertyByMobileNumber(
        @Query("phone") phone: String?
        , @Query("includeRate") includeRate: String?
    ): Call<PropertyResponse>

    @GET("propertyType")
    fun getPropertyTypes(): Call<ApiListResponse<PropertyType>>


    @GET("propertyCategory")
    fun getCategories(): Call<ApiListResponse<PropertyCategory>>

    @GET("propertyUsageType")
    fun getUsageTypes(): Call<ApiListResponse<PropertyUsageType>>

    @POST("properties")
    fun createProperty(
        @Body request: NewProperty
    ): Call<ApiObjectResponse<Any>>


}