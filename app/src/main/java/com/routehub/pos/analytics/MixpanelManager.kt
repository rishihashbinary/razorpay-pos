package com.routehub.pos.analytics

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

object MixpanelManager {

    private var mixpanel: MixpanelAPI? = null

    fun initialize(context: Context, token: String) {
        mixpanel = MixpanelAPI.getInstance(context, token, true)
        mixpanel?.setEnableLogging(true)

        Log.d("MixpanelManager", "Mixpanel initialized successfully");

        val props = JSONObject()
        props.put("platform", "pos")
        props.put("device_type", "razorpay_pos")
//        props.put("app_version", BuildConfig.VERSION_NAME)

        mixpanel?.registerSuperProperties(props)
    }

    fun track(event: String, data: Any? = null) {
        try {
            val props = if (data != null) {
                JSONObject(Gson().toJson(data))
            } else {
                JSONObject()
            }
            mixpanel?.track(event, props)
        } catch (e: Exception) {
            Log.e("MixpanelManager", "Tracking failed: ${e.message}")
        }
    }

    fun trackError(event: String, throwable: Throwable) {

        val props = JSONObject()

        props.put("errorMessage", throwable.message ?: "Unknown Error")
        props.put("errorType", throwable.javaClass.simpleName)
        props.put("stackTrace", throwable.stackTraceToString())
        props.put("app", "POS")

        mixpanel?.track(event, props)
    }

    fun identify(userId: String) {
        mixpanel?.identify(userId)
    }

    fun flush() {
        mixpanel?.flush()
    }
}