package com.routehub.pos.evidence

import com.routehub.pos.models.responses.ApiObjectResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DenialEvidenceService {
    @Multipart
    @POST("denial-evidence")
    fun uploadEvidence(
        @Part("evidence") evidenceJson: RequestBody,
        @Part photo: MultipartBody.Part?,
        @Part audio: MultipartBody.Part?
    ): Call<ApiObjectResponse<Any>>
}