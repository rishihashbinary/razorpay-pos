package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.routehub.pos.R
import com.routehub.pos.data.SampleData
import com.routehub.pos.payments.PaymentLauncher


class PropertyDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val qrCode = intent.getStringExtra("qrCode")

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_property_details)

        val txtName = findViewById<TextView>(R.id.txtName)
        val txtQRCode = findViewById<TextView>(R.id.txtQRCode)
        val txtPhone = findViewById<TextView>(R.id.txtPhone)
        val txtType = findViewById<TextView>(R.id.txtType)
        val txtCategory = findViewById<TextView>(R.id.txtCategory)
        val txtUsage = findViewById<TextView>(R.id.txtUsage)
        val txtFee = findViewById<TextView>(R.id.txtFee)
        val btnPayment = findViewById<Button>(R.id.btnPayment)



        val property = SampleData.sampleProperty

        txtQRCode.text = property.qrCode
        txtName.text = property.name
        txtPhone.text = property.mobileNo

        txtType.text = property.propertyType
        txtCategory.text = property.propertyCategory
        txtUsage.text = property.usageType

        txtFee.text = "₹ ${property.feeAmount}"

        btnPayment.setOnClickListener {

            PaymentLauncher.startPayment(
                this,
                property.feeAmount
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PaymentLauncher.REQUEST_CODE_PAYMENT) {

            if (resultCode == RESULT_OK) {

                val transactionId = data?.getStringExtra("transactionId")

                val status = data?.getStringExtra("status")

            }

        }

    }

}