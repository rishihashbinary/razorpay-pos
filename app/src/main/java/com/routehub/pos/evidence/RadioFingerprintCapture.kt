package com.routehub.pos.evidence

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

data class CellObservation(
    val cellId: String?,
    val type: String,
    val signalStrengthDbm: Int?,
    val isRegistered: Boolean
)

data class WifiObservation(
    val bssid: String,
    val rssi: Int,
    val ssid: String?
)

data class RadioFingerprint(
    val cells: List<CellObservation>,
    val wifiNetworks: List<WifiObservation>,
    val capturedAtMs: Long
)

/**
 * Captures a snapshot of the visible radio environment (cell towers + WiFi
 * networks) at denial time. Used server-side as an anti-spoof cross-check
 * against a per-property baseline learned over repeated visits - a denial
 * whose radio fingerprint doesn't match, even when GPS claims on-site, is
 * the mock-GPS tell.
 *
 * Fail-closed: any read failure (missing permission, radio off, exception)
 * yields an empty list for that source rather than throwing. A fingerprint
 * with nothing observed is still shipped - the manifest explains why.
 */
object RadioFingerprintCapture {

    fun capture(context: Context): RadioFingerprint {
        val appContext = context.applicationContext
        return RadioFingerprint(
            cells = captureCells(appContext),
            wifiNetworks = captureWifi(appContext),
            capturedAtMs = System.currentTimeMillis()
        )
    }

    private fun captureCells(context: Context): List<CellObservation> {
        if (!hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) return emptyList()

        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                ?: return emptyList()

            val cellInfoList = telephonyManager.allCellInfo ?: return emptyList()
            cellInfoList.mapNotNull { extractCellObservation(it) }
        } catch (e: SecurityException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractCellObservation(cellInfo: CellInfo): CellObservation? {
        return try {
            when (cellInfo) {
                is CellInfoLte -> CellObservation(
                    cellId = safeCellId(cellInfo.cellIdentity.ci),
                    type = "LTE",
                    signalStrengthDbm = safeDbm(cellInfo.cellSignalStrength.dbm),
                    isRegistered = cellInfo.isRegistered
                )
                is CellInfoGsm -> CellObservation(
                    cellId = safeCellId(cellInfo.cellIdentity.cid),
                    type = "GSM",
                    signalStrengthDbm = safeDbm(cellInfo.cellSignalStrength.dbm),
                    isRegistered = cellInfo.isRegistered
                )
                is CellInfoWcdma -> CellObservation(
                    cellId = safeCellId(cellInfo.cellIdentity.cid),
                    type = "WCDMA",
                    signalStrengthDbm = safeDbm(cellInfo.cellSignalStrength.dbm),
                    isRegistered = cellInfo.isRegistered
                )
                is CellInfoCdma -> CellObservation(
                    cellId = safeCellId(cellInfo.cellIdentity.basestationId),
                    type = "CDMA",
                    signalStrengthDbm = safeDbm(cellInfo.cellSignalStrength.dbm),
                    isRegistered = cellInfo.isRegistered
                )
                else -> CellObservation(
                    // Covers CellInfoNr (5G, API 29+) and any future type -
                    // recorded generically rather than skipped, since even
                    // "a cell of some type was visible" has anti-spoof value.
                    cellId = null,
                    type = cellInfo.javaClass.simpleName,
                    signalStrengthDbm = null,
                    isRegistered = cellInfo.isRegistered
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun safeCellId(value: Int): String? {
        return if (value == Int.MAX_VALUE || value < 0) null else value.toString()
    }

    private fun safeDbm(value: Int): Int? {
        return if (value == Int.MAX_VALUE) null else value
    }

    private fun captureWifi(context: Context): List<WifiObservation> {
        if (!hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) return emptyList()
        if (!hasPermission(context, Manifest.permission.ACCESS_WIFI_STATE)) return emptyList()

        return try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return emptyList()

            if (!wifiManager.isWifiEnabled) return emptyList()

            // Reads the most recent scan the OS already has cached, rather than
            // triggering a new one - startScan() is throttled/deprecated on
            // modern Android, and a recent cached scan is fine for a fingerprint.
            val results = wifiManager.scanResults ?: return emptyList()
            results.map { result ->
                WifiObservation(
                    bssid = result.BSSID ?: "",
                    rssi = result.level,
                    ssid = result.SSID
                )
            }
        } catch (e: SecurityException) {
            emptyList()
        } catch (e: Exception) {
            emptyList()
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