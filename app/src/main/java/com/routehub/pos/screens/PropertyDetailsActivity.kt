package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.routehub.pos.R
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.data.SampleData
import com.routehub.pos.models.responses.PropertyResponse
import com.routehub.pos.payments.PaymentLauncher
import com.routehub.pos.services.PropertiesService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.eze.api.EzeAPI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.routehub.pos.analytics.AnalyticsTracker
import com.routehub.pos.analytics.MixpanelManager
import org.json.JSONObject
import org.json.JSONArray


class PropertyDetailsActivity : AppCompatActivity() {

    val apiService = ApiClient.retrofit.create(PropertiesService::class.java)
    private val REQUEST_CODE_PAY = 10016
//    private var googleMap: GoogleMap? = null


    fun startPayment(amount: Double, orderId: String) {

        try {

            val jsonRequest = JSONObject()

            val options = JSONObject()
            val references = JSONObject()
            val customer = JSONObject()

            // Order reference
            references.put("reference1", orderId)

            // Customer info (optional)
            customer.put("name", "Citizen")
            customer.put("mobileNo", "9999999999")
            customer.put("email", "citizen@example.com")

            options.put("references", references)
            options.put("customer", customer)
            options.put("amountCashback", 0)
            options.put("amountTip", 0)

            jsonRequest.put("amount", amount)
            jsonRequest.put("options", options)

            MixpanelManager.track("Initiating Payment", jsonRequest)

            // Call Razorpay POS unified payment screen
            EzeAPI.pay(this, REQUEST_CODE_PAY, jsonRequest)

        } catch (e: Exception) {
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
            AnalyticsTracker.paymentFailed(orderId, e.message.toString())
            e.printStackTrace()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        val qrCode = intent.getStringExtra("qrCode")

        println("Fetching details for QR code: $qrCode")

        val props = JSONObject()
        props.put("qrCode", qrCode)
        MixpanelManager.track("Loading Property Using QR", props)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_property_details)

        // Map
//        val mapFragment = SupportMapFragment.newInstance()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.mapContainer, mapFragment)
//            .commit()
//
//        mapFragment.getMapAsync(this)

        val txtName = findViewById<TextView>(R.id.txtName)
        val txtQRCode = findViewById<TextView>(R.id.txtQRCode)
        val txtPhone = findViewById<TextView>(R.id.txtPhone)
        val txtType = findViewById<TextView>(R.id.txtType)
        val txtCategory = findViewById<TextView>(R.id.txtCategory)
        val txtUsage = findViewById<TextView>(R.id.txtUsage)
        val txtFee = findViewById<TextView>(R.id.txtFee)
        val btnPayment = findViewById<Button>(R.id.btnPayment)

        apiService.getPropertyByQr(qrCode).enqueue(object : Callback<PropertyResponse> {

            override fun onResponse(
                call: Call<PropertyResponse>,
                response: Response<PropertyResponse>
            ) {

                if (response.isSuccessful) {

                    val property = response.body()?.data?.data[0]

                    property?.let {
                        txtName.text = it.name ?: it.address1
//                        addressText.text = it.address
//                        amountText.text = "₹${it.pendingAmount}"
                    }


                    MixpanelManager.track("Property Details", property)
                }
            }

            override fun onFailure(call: Call<PropertyResponse>, t: Throwable) {
                MixpanelManager.trackError("Property Loading Failed", t)
                t.printStackTrace()
            }
        })



        val property = SampleData.sampleProperty

        txtQRCode.text = property.qrCode
        txtName.text = property.name
        txtPhone.text = property.mobileNo

        txtType.text = property.propertyTypeId?.typeName
        txtCategory.text = property.propertyCategoryId?.categoryName
        txtUsage.text = property.propertyUsageTypeId?.typeName

        txtFee.text = "₹ 1000"

        btnPayment.setOnClickListener {
            MixpanelManager.track("Payment Button Clicked")
            startPayment(250.0, "ORDER123")

//            PaymentLauncher.startPayment(
//                this,
//                1
//            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)
        val paymentResult = JSONObject()

        val transactionId = data?.getStringExtra("transactionId")
        val status = data?.getStringExtra("status")

        paymentResult.put("requestCode", requestCode)
        paymentResult.put("resultCode", resultCode)
        paymentResult.put("transactionId", transactionId)
        paymentResult.put("status", status)

        MixpanelManager.track("Payment Result", paymentResult)

        if (requestCode == PaymentLauncher.REQUEST_CODE_PAYMENT) {

            if (resultCode == RESULT_OK) {

//                val transactionId = data?.getStringExtra("transactionId")
//
//                val status = data?.getStringExtra("status")
                Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
                MixpanelManager.track("Payment Successful", paymentResult)

            }

        }

    }

//    override fun onMapReady(map: GoogleMap) {
//
//        googleMap = map
//
//        // Example coordinates (replace with property coordinates)
//        val propertyLocation = LatLng(19.0760, 72.8777)
//
//        googleMap?.addMarker(
//            MarkerOptions()
//                .position(propertyLocation)
//                .title("Property Location")
//        )
//
//        googleMap?.moveCamera(
//            CameraUpdateFactory.newLatLngZoom(propertyLocation, 17f)
//        )
//    }

}