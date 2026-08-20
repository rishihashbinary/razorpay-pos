package com.routehub.pos.evidence

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

/**
 * Best-effort device + app identity for the evidence bundle's Context block.
 *
 * True hardware serial (Build.getSerial()) is restricted on modern Android
 * to privileged/device-owner apps and requires READ_PHONE_STATE even then -
 * that permission is deliberately NOT requested here, to avoid adding a new
 * dangerous-permission prompt for this alone. Settings.Secure.ANDROID_ID is
 * used as the practical fallback: stable per device+app, no extra permission.
 * If this fleet's terminals are device-owner provisioned and a true serial
 * is required, tryGetHardwareSerial() below is where that would plug in.
 */
object DeviceContext {

    fun getDeviceIdentifier(context: Context): String {
        val serial = tryGetHardwareSerial()
        if (serial != null) return serial

        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun tryGetHardwareSerial(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val serial = Build.getSerial()
                if (serial.isNullOrBlank() || serial == Build.UNKNOWN) null else serial
            } else {
                @Suppress("DEPRECATION")
                val serial = Build.SERIAL
                if (serial.isNullOrBlank() || serial == Build.UNKNOWN) null else serial
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}