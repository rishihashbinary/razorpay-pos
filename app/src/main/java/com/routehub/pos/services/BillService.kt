package com.routehub.pos.services

import com.routehub.pos.models.DirectCollection
import com.routehub.pos.models.responses.ApiListResponse
import com.routehub.pos.models.responses.ApiObjectResponse
import com.routehub.pos.models.responses.BillData
import com.routehub.pos.models.responses.PropertyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BillService {
    @GET("properties/details/page/1")
    fun updatePaymentStatus(
        @Query("qrCode") qrCode: String?
    ): Call<PropertyResponse>

    @POST("transaction/direct-collection")
    fun createDirectCollection(
        @Body request: DirectCollection
    ): Call<ApiListResponse<Any>>

    @POST("transaction/deny")
    fun denyPayment(@Body request: DirectCollection): Call<ApiListResponse<Any>>

    @GET("bill/property/{propertyId}")
    fun getPropertyBills(
        @Path("propertyId") propertyId: String?,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): Call<ApiObjectResponse<BillData>>


}