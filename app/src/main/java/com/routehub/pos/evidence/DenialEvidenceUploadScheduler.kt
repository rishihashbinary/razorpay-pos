package com.routehub.pos.evidence

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

/**
 * Schedules DenialEvidenceUploadWorker.
 *
 * - triggerNow(): fired right after a denial is enqueued, for fast delivery
 *   when the network is already up.
 * - schedulePeriodic(): a safety net registered once per app launch, in
 *   case triggerNow() never got the chance to run (app killed, offline at
 *   submission time, etc). Safe to call on every launch - WorkManager
 *   de-dupes by unique work name via KEEP.
 */
object DenialEvidenceUploadScheduler {

    private const val ONE_TIME_WORK_NAME = "denial_evidence_upload_now"
    private const val PERIODIC_WORK_NAME = "denial_evidence_upload_periodic"
    private const val PERIODIC_INTERVAL_MINUTES = 15L

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun triggerNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<DenialEvidenceUploadWorker>()
            .setConstraints(networkConstraint)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<DenialEvidenceUploadWorker>(
            PERIODIC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(networkConstraint)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}