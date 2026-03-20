package com.routehub.pos.helpers

import android.content.Context

object PlayHelper {
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val status = com.google.android.gms.common.GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)
        return status == com.google.android.gms.common.ConnectionResult.SUCCESS
    }
}