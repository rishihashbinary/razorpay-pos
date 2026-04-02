package com.routehub.pos.services

import com.routehub.pos.models.Ward
import com.routehub.pos.models.Zone
import com.routehub.pos.models.responses.ApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface WardService {
    @GET("wards")
    fun getWards(): Call<ApiResponse<Ward>>
}