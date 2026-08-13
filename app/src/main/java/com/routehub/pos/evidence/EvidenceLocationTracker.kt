package com.routehub.pos.evidence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat


class EvidenceLocationTracker(private val context: Context) {

    data class LocationFix(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float,
        val provider: String,
        val isMock: Boolean,
        val fixTimestamp: Long
    ) {
        /** Age of this fix relative to now, in milliseconds. Computed at read time. */
        fun ageMs(): Long = System.currentTimeMillis() - fixTimestamp
    }

    private val locationManager: LocationManager? =
        context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val track = mutableListOf<LocationFix>()
    private var dwellStartMs: Long? = null
    private var isTracking = false

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            onFixReceived(location)
        }
    }

    @Synchronized
    fun start() {
        if (isTracking) return
        if (!hasLocationPermission()) return
        val manager = locationManager ?: return

        dwellStartMs = System.currentTimeMillis()

        for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
            try {
                if (manager.isProviderEnabled(provider)) {
                    manager.requestLocationUpdates(
                        provider,
                        MIN_UPDATE_INTERVAL_MS,
                        MIN_UPDATE_DISTANCE_M,
                        listener
                    )
                }
            } catch (e: SecurityException) {

            } catch (e: IllegalArgumentException) {

            }
        }

        seedFromLastKnownLocation(manager)
        isTracking = true
    }


    @Synchronized
    fun stop() {
        if (!isTracking) return
        try {
            locationManager?.removeUpdates(listener)
        } catch (e: SecurityException) {
            // Already lost permission - nothing to clean up.
        }
        isTracking = false
    }


    @Synchronized
    fun getBestFix(): LocationFix? = track.lastOrNull()


    @Synchronized
    fun getTrack(): List<LocationFix> = track.toList()

    fun getDwellSeconds(): Long? {
        val startedAt = dwellStartMs ?: return null
        return (System.currentTimeMillis() - startedAt) / 1000
    }

    @Synchronized
    private fun onFixReceived(location: Location) {
        val fix = LocationFix(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = if (location.hasAccuracy()) location.accuracy else -1f,
            provider = location.provider ?: "unknown",
            isMock = location.isFromMockProvider,
            fixTimestamp = location.time
        )

        track.add(fix)
        if (track.size > MAX_TRACK_SIZE) {
            track.removeAt(0)
        }
    }

    private fun seedFromLastKnownLocation(manager: LocationManager) {
        try {
            for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
                val last = manager.getLastKnownLocation(provider)
                if (last != null) {
                    onFixReceived(last)
                }
            }
        } catch (e: SecurityException) {
            // No permission - nothing to seed with.
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val MIN_UPDATE_INTERVAL_MS = 5000L   // 5s between fixes
        private const val MIN_UPDATE_DISTANCE_M = 5f        // or 5m of movement
        private const val MAX_TRACK_SIZE = 20                // cap the in-memory track
    }
}