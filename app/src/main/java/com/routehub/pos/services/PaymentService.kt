package com.routehub.pos.services

import com.routehub.pos.models.Payment
import retrofit2.http.Body
import retrofit2.http.POST

interface PaymentService {

    @POST("payments/create")
    fun createPayment(@Body payment: Payment)
}