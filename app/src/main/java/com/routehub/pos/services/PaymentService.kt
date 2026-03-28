package com.routehub.pos.services

import com.routehub.pos.models.Payment
import com.routehub.pos.models.Reason
import com.routehub.pos.models.responses.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentService {

    @POST("payments/create")
    fun createPayment(@Body payment: Payment)
    
    @GET("reasons")
    fun getDenialReasons(): Call<ApiResponse<Reason>>
}



























