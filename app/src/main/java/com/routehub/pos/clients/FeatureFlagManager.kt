package com.routehub.pos.clients

import android.util.Log
import com.routehub.pos.models.responses.ApiObjectResponse
import com.routehub.pos.models.responses.ConfigData
import com.routehub.pos.services.ConfigService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FeatureFlagManager {

    private const val TAG = "FeatureFlagManager"

    private const val KEY_ALLOW_RECEIPT_REPRINT = "allow-receipt-reprint"
    private const val KEY_ALLOW_BILL_REJECTION = "allow-bill-rejection"

    @Volatile
    var allowReceiptReprint: Boolean = false
        private set

    @Volatile
    var allowBillRejection: Boolean = false
        private set

    fun refresh(configService: ConfigService) {
        fetchFlag(configService, KEY_ALLOW_RECEIPT_REPRINT) { allowReceiptReprint = it }
        fetchFlag(configService, KEY_ALLOW_BILL_REJECTION) { allowBillRejection = it }
    }

    private fun fetchFlag(
        configService: ConfigService,
        key: String,
        onResult: (Boolean) -> Unit
    ) {
        configService.getConfig(key).enqueue(object : Callback<ApiObjectResponse<ConfigData>> {
            override fun onResponse(
                call: Call<ApiObjectResponse<ConfigData>>,
                response: Response<ApiObjectResponse<ConfigData>>
            ) {
                if (!response.isSuccessful) {
                    Log.w(TAG, "Config '$key' fetch failed: HTTP ${response.code()}")
                    onResult(false)
                    return
                }

                // `status` is intentionally ignored — only data.value decides the outcome.
                val rawValue = response.body()?.data?.value
                val enabled = rawValue?.trim()?.equals("true", ignoreCase = true) == true
                Log.d(TAG, "Config '$key' resolved to $enabled (raw='$rawValue')")
                onResult(enabled)
            }

            override fun onFailure(call: Call<ApiObjectResponse<ConfigData>>, t: Throwable) {
                Log.w(TAG, "Config '$key' fetch error: ${t.message}")
                onResult(false)
            }
        })
    }
}