package com.routehub.pos.analytics

import android.content.Context
import android.util.Log
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

object MixpanelManager {

    private var mixpanel: MixpanelAPI? = null

    fun initialize(context: Context, token: String) {
        mixpanel = MixpanelAPI.getInstance(context, token, true)

        val props = JSONObject()
        props.put("platform", "pos")
        props.put("device_type", "razorpay_pos")
//        props.put("app_version", BuildConfig.VERSION_NAME)

        mixpanel?.registerSuperProperties(props)
    }

    fun track(event: String, props: JSONObject? = null) {
        try {
            mixpanel?.track(event, props)
        } catch (e: Exception) {
            Log.e("MixpanelManager", "Tracking failed: ${e.message}")
        }
    }

    fun identify(userId: String) {
        mixpanel?.identify(userId)
    }

    fun flush() {
        mixpanel?.flush()
    }
}