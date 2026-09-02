package com.routehub.pos.clients

import android.util.Log
import com.routehub.pos.models.responses.ApiObjectResponse
import com.routehub.pos.models.responses.ConfigData
import com.routehub.pos.services.ConfigService
import com.routehub.pos.utils.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object FeatureFlagManager {

    private const val TAG = "FeatureFlagManager"

    private const val KEY_ALLOW_RECEIPT_REPRINT = "allow-receipt-reprint"
    private const val KEY_ALLOW_BILL_REJECTION = "allow-bill-rejection"
    private const val KEY_ALLOW_FEE_UPDATE = "ucc-allow-fee-update"
    private const val FORCE_ALLOW_FEE_UPDATE = true

    val allowReceiptReprint: Boolean get() = cached(KEY_ALLOW_RECEIPT_REPRINT)
    val allowBillRejection: Boolean get() = cached(KEY_ALLOW_BILL_REJECTION)
    val allowFeeUpdate: Boolean
        get() = FORCE_ALLOW_FEE_UPDATE || cached(KEY_ALLOW_FEE_UPDATE)

    fun refresh(configService: ConfigService) {
        fetchFlag(configService, KEY_ALLOW_RECEIPT_REPRINT)
        fetchFlag(configService, KEY_ALLOW_BILL_REJECTION)
        fetchFlag(configService, KEY_ALLOW_FEE_UPDATE)
    }

    private fun cached(key: String): Boolean = Session.getBoolean("flag_$key", false)

    private fun store(key: String, enabled: Boolean) = Session.store("flag_$key", enabled)

    private fun fetchFlag(configService: ConfigService, key: String) {
        configService.getConfig(key).enqueue(object : Callback<ApiObjectResponse<ConfigData>> {
            override fun onResponse(
                call: Call<ApiObjectResponse<ConfigData>>,
                response: Response<ApiObjectResponse<ConfigData>>
            ) {
                if (!response.isSuccessful) {
                    Log.w(TAG, "Config '$key' fetch failed: HTTP ${response.code()}")
                    store(key, false)
                    return
                }

                val rawValue = response.body()?.data?.value
                val enabled = rawValue?.trim()?.equals("true", ignoreCase = true) == true
                Log.d(TAG, "Config '$key' resolved to $enabled (raw='$rawValue')")
                store(key, enabled)
            }

            override fun onFailure(call: Call<ApiObjectResponse<ConfigData>>, t: Throwable) {
                Log.w(TAG, "Config '$key' fetch error: ${t.message}")
                store(key, false)
            }
        })
    }
}