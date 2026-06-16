package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.routehub.pos.analytics.MixpanelManager
import com.routehub.pos.helpers.QrHelper

class ScanQrActivity : AppCompatActivity() {

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            finish()
        } else {
            val qrCode = QrHelper.extractQrCode(result.contents)
            Log.d("ScanQrActivity", "QR Code: $qrCode")
            MixpanelManager.track("ScanQrActivity::QR Code: $qrCode")
            if (qrCode != null) {
                val intent = Intent(this, PropertyDetailsActivity::class.java).apply {
                    putExtra("qrCode", qrCode)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid QR code.", Toast.LENGTH_LONG).show()
                Log.d("ScanQrActivity", "Invalid QR code")
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MixpanelManager.track("ScanQrActivity::onCreate")
        if (savedInstanceState == null) {
            MixpanelManager.track("ScanQrActivity::savedInstanceState is NULL")
            startScanner()
        } else {
            MixpanelManager.track("ScanQrActivity::onCreate::finish")
            finish()
        }
    }

    private fun startScanner() {
        MixpanelManager.track("ScanQrActivity::starting scanner")
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Scan Property QR Code")
            setBeepEnabled(true)
            setOrientationLocked(false)
        }
        barcodeLauncher.launch(options)
    }
}