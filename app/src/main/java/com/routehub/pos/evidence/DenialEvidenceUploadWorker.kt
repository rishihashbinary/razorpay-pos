package com.routehub.pos.evidence

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.routehub.pos.clients.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class DenialEvidenceUploadWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val service = ApiClient.retrofit.create(DenialEvidenceService::class.java)
    private val gson = Gson()

    override fun doWork(): Result {
        val pending = DenialEvidenceQueue.getPending(applicationContext)
        if (pending.isEmpty()) return Result.success()

        var anyFailure = false

        for (item in pending) {
            val evidence = item.evidence
            DenialEvidenceQueue.recordAttempt(applicationContext, evidence.clientTransactionId)

            val success = try {
                uploadOne(evidence)
            } catch (e: Exception) {
                false
            }

            if (success) {
                DenialEvidenceQueue.purgeUploaded(applicationContext, evidence.clientTransactionId)
            } else {
                anyFailure = true
            }
        }
        return if (anyFailure) Result.retry() else Result.success()
    }

    private fun uploadOne(evidence: DenialEvidence): Boolean {
        val envelopeJson = buildEnvelopeJson(evidence)
        val envelopeBody = envelopeJson.toRequestBody("application/json".toMediaTypeOrNull())

        val photoPart = evidence.media
            .firstOrNull { it.type == MediaType.PHOTO && it.filePath != null }
            ?.let { filePart("photo", it.filePath!!, it.mimeType ?: "image/jpeg") }

        val audioPart = evidence.media
            .firstOrNull { it.type == MediaType.AUDIO && it.filePath != null }
            ?.let { filePart("audio", it.filePath!!, it.mimeType ?: "audio/mp4") }

        val response = service.uploadEvidence(envelopeBody, photoPart, audioPart).execute()
        return response.isSuccessful
    }

    private fun filePart(fieldName: String, path: String, mimeType: String): MultipartBody.Part? {
        val file = File(path)
        if (!file.exists()) return null

        val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(fieldName, file.name, requestBody)
    }
    private fun buildEnvelopeJson(evidence: DenialEvidence): String {
        val envelopeMedia = evidence.media.map { item ->
            mapOf(
                "type" to item.type.name,
                "mimeType" to item.mimeType,
                "capturedReason" to item.capturedReason?.name,
                "present" to (item.filePath != null)
            )
        }

        val envelope = mapOf(
            "clientTransactionId" to evidence.clientTransactionId,
            "reasonCode" to evidence.reasonCode,
            "remarks" to evidence.remarks,
            "geo" to evidence.geo,
            "geoTrack" to evidence.geoTrack,
            "dwellSeconds" to evidence.dwellSeconds,
            "radioFingerprint" to evidence.radioFingerprint,
            "capabilities" to evidence.capabilities,
            "media" to envelopeMedia,
            "deviceSerial" to evidence.deviceSerial,
            "operatorId" to evidence.operatorId,
            "appVersion" to evidence.appVersion,
            "createdAtMs" to evidence.createdAtMs
        )

        return gson.toJson(envelope)
    }
}