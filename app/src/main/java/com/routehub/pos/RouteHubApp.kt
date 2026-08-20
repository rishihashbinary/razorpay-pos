package com.routehub.pos

import android.app.Application
import android.content.Context
import com.routehub.pos.evidence.DenialEvidenceUploadScheduler
import com.routehub.pos.utils.Session

class RouteHubApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Session.init(this)
        appContext = applicationContext
        DenialEvidenceUploadScheduler.schedulePeriodic(this)
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}