package com.routehub.pos.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.routehub.pos.helpers.PlayHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

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

    // ✅ Get Last Known Location (fast but may be null)
    fun getLastLocation(onResult: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                onResult(location)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // ✅ Get Fresh Location (recommended)
    fun getCurrentLocation(onResult: (Location?) -> Unit) {

        if (!hasLocationPermission()) {
            onResult(null)
            return
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1 // 🔥 Only one update needed
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                onResult(location)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    suspend fun getCurrentLocationSuspend(): Location? =
        suspendCancellableCoroutine { continuation ->

            if (!hasLocationPermission()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                numUpdates = 1
            }

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    continuation.resume(result.lastLocation)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )
        }

    @SuppressLint("MissingPermission")
    fun getLocationUsingLocationManager(onResult: (Location?) -> Unit) {

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val providers = locationManager.getProviders(true)

        var bestLocation: Location? = null

        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }
        }

        if (bestLocation != null) {
            onResult(bestLocation)
            return
        }

        // If no last known → request update
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onResult(location)
                locationManager.removeUpdates(this)
            }
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                listener
            )
        } catch (e: Exception) {
            onResult(null)
        }
    }

    fun getLocation(
        onResult: (Location?) -> Unit
    ) {
        if (PlayHelper.isGooglePlayServicesAvailable(context)) {
            getCurrentLocation(onResult)
        } else {
            getLocationUsingLocationManager(onResult)
        }
    }
}