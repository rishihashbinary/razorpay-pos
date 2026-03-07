package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

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

                val intent = Intent(this, PropertyDetailsActivity::class.java)

                intent.putExtra("qrCode", qrValue)

                startActivity(intent)

                finish()

            } else {

                finish()

            }

        } else {

            super.onActivityResult(requestCode, resultCode, data)

        }
    }
}