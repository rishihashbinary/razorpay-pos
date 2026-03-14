package com.routehub.pos.analytics

import org.json.JSONObject

object AnalyticsTracker {

    fun paymentStarted(amount: Int, propertyId: String) {

        val props = JSONObject().apply {
            put("amount", amount)
            put("property_id", propertyId)
        }

        MixpanelManager.track(
            AnalyticsEvents.PAYMENT_STARTED,
            props
        )
    }

    fun paymentSuccess(orderId: String, amount: Int) {

        val props = JSONObject().apply {
            put("order_id", orderId)
            put("amount", amount)
        }

        MixpanelManager.track(
            AnalyticsEvents.PAYMENT_SUCCESS,
            props
        )
    }

    fun paymentFailed(orderId: String, reason: String) {

        val props = JSONObject().apply {
            put("order_id", orderId)
            put("reason", reason)
        }

        MixpanelManager.track("payment_failed", props)
    }

    fun paymentCancelled(orderId: String, reason: String) {

        val props = JSONObject().apply {
            put("order_id", orderId)
            put("reason", reason)
        }

        MixpanelManager.track("payment_cancelled", props)
    }

    fun billFetched(propertyId: String, success: Boolean) {

        val props = JSONObject().apply {
            put("property_id", propertyId)
            put("success", success)
        }

        MixpanelManager.track("bill_fetched", props)
    }

    fun billUpdated(propertyId: String, amount: Int) {

        val props = JSONObject().apply {
            put("property_id", propertyId)
            put("amount", amount)
        }

        MixpanelManager.track("bill_updated", props)
    }

}