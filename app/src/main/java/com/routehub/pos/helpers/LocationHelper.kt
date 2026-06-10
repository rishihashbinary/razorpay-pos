@file:OptIn(ExperimentalCoroutinesApi::class)

package com.routehub.pos.helpers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val handler = Handler(Looper.getMainLooper())
    private val listeners = mutableListOf<LocationListener>()

    private var resolved = false

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    // ✅ Check Permission
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ✅ Request Permission
    fun requestPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun getCurrentLocation(
        timeout: Long = 10_000L,
        onResult: (Location?) -> Unit
    ) {
        resolved = false

        // Step 1: Try last known location
        val lastLocation = getBestLastKnownLocation()
        if (lastLocation != null) {
            onResult(lastLocation)
            return
        }

        // Step 2: Get preferred providers (POS-friendly priority)
        val providers = getAvailableProviders()

        if (providers.isEmpty()) {
            onResult(null)
            return
        }

        // Step 3: Setup timeout
        val timeoutRunnable = Runnable {
            if (!resolved) {
                resolved = true
                clearListeners()
                onResult(null)
            }
        }

        // Step 4: Listen for updates
        providers.forEach { provider ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!resolved) {
                        resolved = true
                        handler.removeCallbacks(timeoutRunnable)
                        clearListeners()
                        onResult(location)
                    }
                }

                override fun onProviderDisabled(provider: String) {}
                override fun onProviderEnabled(provider: String) {}

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) {}
            }

            try {
                locationManager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
                listeners.add(listener)
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }

        if (listeners.isEmpty()) {
            onResult(null)
            return
        }

        handler.postDelayed(timeoutRunnable, timeout)
    }

    private fun getBestLastKnownLocation(): Location? {
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            val location = try {
                locationManager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            } ?: continue

            if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                bestLocation = location
            }
        }

        return bestLocation
    }

    private fun getAvailableProviders(): List<String> {
        return listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).filter { locationManager.isProviderEnabled(it) }
    }

    fun clearListeners() {
        listeners.forEach {
            try {
                locationManager.removeUpdates(it)
            } catch (_: Exception) {
            }
        }
        listeners.clear()
    }

    suspend fun getCurrentLocationSuspend(
        timeout: Long = 10_000L
    ): Location? = suspendCancellableCoroutine { continuation ->

        getCurrentLocation(timeout) { location ->
            if (continuation.isActive) {
                continuation.resume(location)
            }
        }

        // Handle coroutine cancellation (VERY IMPORTANT)
        continuation.invokeOnCancellation {
            clearListeners()
        }
    }
}