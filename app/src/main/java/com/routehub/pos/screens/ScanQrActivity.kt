package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.routehub.pos.helpers.QrHelper

class ScanQrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        startScanner()

    }

    private fun startScanner() {

        val integrator = IntentIntegrator(this)

        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan Property QR Code")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)

        integrator.initiateScan()

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        val result = IntentIntegrator.parseActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (result != null) {

            if (result.contents != null) {

                val qrValue = result.contents



                val qrCode = QrHelper.extractQrCode(qrValue)

                Log.d("ScanQrActivity", "QR Code: $qrCode")

                if(qrCode != null) {

                    val intent = Intent(this, PropertyDetailsActivity::class.java)
                    intent.putExtra("qrCode", qrValue)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid QR code.", Toast.LENGTH_LONG).show()
                    Log.d("ScanQrActivity", "Invalid QR code")
                }

                finish()

            } else {

                finish()

            }

        } else {

            super.onActivityResult(requestCode, resultCode, data)

        }
    }
}