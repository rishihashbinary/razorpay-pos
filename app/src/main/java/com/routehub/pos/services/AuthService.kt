package com.routehub.pos.services

import com.routehub.pos.models.LoginRequest
import com.routehub.pos.models.responses.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {

    @POST("users/login")
    fun login(
        @Query("accessControlListFormatted") formatted: Boolean = true,
        @Query("accessControlListEncrypted") encrypted: Boolean = false,
        @Header("client") client: String = "pos",
        @Body request: LoginRequest
    ): Call<LoginResponse>
}