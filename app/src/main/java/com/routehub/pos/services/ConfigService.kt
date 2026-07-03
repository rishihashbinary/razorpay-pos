package com.routehub.pos.services

import com.routehub.pos.models.responses.ApiObjectResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ConfigService {
    @GET("configurations/key/{key}")
    fun getConfig(@Path("key") key: String): Call<ApiObjectResponse<Any>>
}