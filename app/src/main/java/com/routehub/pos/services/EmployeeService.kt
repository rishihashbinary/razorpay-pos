package com.routehub.pos.services

import com.routehub.pos.models.EmployeeDetail
import com.routehub.pos.models.responses.ApiObjectResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface EmployeeService {
    @GET("employees/{employeeId}")
    fun getEmployeeDetail(@Path("employeeId") employeeId: String): Call<ApiObjectResponse<EmployeeDetail>>
}