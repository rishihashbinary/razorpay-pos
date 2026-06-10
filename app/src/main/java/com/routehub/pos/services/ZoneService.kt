package com.routehub.pos.services

import com.routehub.pos.models.Zone
import com.routehub.pos.models.responses.ApiListResponse
import retrofit2.Call
import retrofit2.http.GET

interface ZoneService {
    @GET("zones")
    fun getZones(): Call<ApiListResponse<Zone>>
}