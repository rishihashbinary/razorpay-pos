package com.routehub.pos.evidence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

data class CapabilityManifest(
    val hasCamera: Boolean,
    val hasMic: Boolean,
    val hasGps: Boolean,
    val hasCameraPermission: Boolean,
    val hasMicPermission: Boolean,
    val hasLocationPermission: Boolean
)


object CapabilityProbe {

    private data class HardwareCapabilities(
        val hasCamera: Boolean,
        val hasMic: Boolean,
        val hasGps: Boolean
    )

    @Volatile
    private var hardwareCache: HardwareCapabilities? = null

    fun resolve(context: Context): CapabilityManifest {
        val appContext = context.applicationContext
        val hardware = hardwareCache ?: detectHardware(appContext).also { hardwareCache = it }

        return CapabilityManifest(
            hasCamera = hardware.hasCamera,
            hasMic = hardware.hasMic,
            hasGps = hardware.hasGps,
            hasCameraPermission = hasPermission(appContext, Manifest.permission.CAMERA),
            hasMicPermission = hasPermission(appContext, Manifest.permission.RECORD_AUDIO),
            hasLocationPermission = hasPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    private fun detectHardware(context: Context): HardwareCapabilities {
        return try {
            val pm = context.packageManager
            HardwareCapabilities(
                hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY),
                hasMic = pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE),
                hasGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
            )
        } catch (e: Exception) {
            HardwareCapabilities(hasCamera = false, hasMic = false, hasGps = false)
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return try {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
}