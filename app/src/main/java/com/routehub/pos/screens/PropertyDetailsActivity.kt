package com.routehub.pos.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toLowerCase
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.eze.api.EzeAPI
import com.routehub.pos.R
import com.routehub.pos.analytics.AnalyticsTracker
import com.routehub.pos.analytics.MixpanelManager
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.clients.SessionManager
import com.routehub.pos.helpers.LocationHelper
import com.routehub.pos.helpers.PlayHelper
import com.routehub.pos.models.CollectionPeriod
import com.routehub.pos.models.DirectCollection
import com.routehub.pos.models.Property
import com.routehub.pos.models.PropertyLocation
import com.routehub.pos.models.responses.ApiResponse
import com.routehub.pos.models.responses.PropertyResponse
import com.routehub.pos.payments.PaymentLauncher
import com.routehub.pos.services.BillService
import com.routehub.pos.services.PropertiesService
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class PropertyDetailsActivity : AppCompatActivity() {

    var property: Property? = null
    var hasLocation: Boolean = false;
    private lateinit var locationHelper: LocationHelper
    lateinit var btnPayment: Button
//    lateinit var txtMessage: TextView

    val apiService = ApiClient.retrofit.create(PropertiesService::class.java)
    private val REQUEST_CODE_PAY = 10016
    private val REQUEST_CODE_PRINT_RECEIPT = 10028
//    private var googleMap: GoogleMap? = null


    fun startPayment(amount: Float?, orderId: String) {

        try {

            val jsonRequest = JSONObject()

            val options = JSONObject()
            val references = JSONObject()
            val customer = JSONObject()

            // Order reference
            references.put("reference1", orderId)
            references.put("propertyId", property?._id)
            references.put("propertyQr", property?.qrCode)

            // Customer info (optional)
            customer.put("name", property?.name ?: property?.ownerName ?: property?.address1)
            customer.put("mobileNo", property?.mobileNo)
//            customer.put("email", property?.email)

            options.put("references", references)
            options.put("customer", customer)
            options.put("amountCashback", 0)
            options.put("amountTip", 0)

            jsonRequest.put("amount", amount)
            jsonRequest.put("options", options)

            MixpanelManager.track("Initiating Payment", jsonRequest)

            // Call Razorpay POS unified payment screen
            EzeAPI.pay(this, PaymentLauncher.REQUEST_CODE_PAYMENT, jsonRequest)

        } catch (e: Exception) {
            Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
            AnalyticsTracker.paymentFailed(orderId, e.message.toString())
            e.printStackTrace()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        val playServiceExists = PlayHelper.isGooglePlayServicesAvailable(this)
//        Toast.makeText(this, "Play Service Exists: $playServiceExists", Toast.LENGTH_LONG).show()


        val qrCode = intent.getStringExtra("qrCode")

        println("Fetching details for QR code: $qrCode")

        val props = JSONObject()
        props.put("qrCode", qrCode)
        MixpanelManager.track("Loading Property Using QR", props)

        locationHelper = LocationHelper(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }

        // Step 1: Check permission
        if (!locationHelper.hasLocationPermission()) {
            Toast.makeText(this, "No location permission found, requesting.", Toast.LENGTH_LONG).show();
            locationHelper.requestPermission(this)
        } else {
//            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
            // Step 2: Get location
            locationHelper.getLocation { location ->
                if (location != null) {
//                    Toast.makeText(this, "Got Location!", Toast.LENGTH_LONG).show();
                    val lat = location.latitude
                    val lng = location.longitude
                    hasLocation = true;
//                    resetMessage()
//                    btnPayment.isEnabled = hasLocation
                    println("Lat: $lat, Lng: $lng")
                } else {
                    Toast.makeText(this, "Cannot fetch location!", Toast.LENGTH_LONG).show();
                    println("Unable to fetch location")
                }
            }
        }

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
//        txtMessage = findViewById<TextView>(R.id.txtMessage)
        btnPayment = findViewById<Button>(R.id.btnPayment)

//        if(!hasLocation) {
//            txtMessage.setText(getString(R.string.please_enable_location_to_continue))
//        }

//        btnPayment.isEnabled = hasLocation


        apiService.getPropertyByQr(qrCode, "true").enqueue(object : Callback<PropertyResponse> {

            override fun onResponse(
                call: Call<PropertyResponse>,
                response: Response<PropertyResponse>
            ) {

                if (response.isSuccessful) {

                    property = response.body()?.data

                    property?.let {
                        txtName.text = it.name ?: it.address1
//                        addressText.text = it.address
//                        amountText.text = "₹${it.pendingAmount}"
                    }

//                    val property = SampleData.sampleProperty

                    txtQRCode.text = property?.qrCode
                    txtName.text = property?.name ?: property?.ownerName ?: property?.address1
                    txtPhone.text = property?.mobileNo

                    txtType.text = property?.propertyTypeId?.typeName
                    txtCategory.text = property?.propertyCategoryId?.categoryName
                    txtUsage.text = property?.propertyUsageTypeId?.typeName

                    txtFee.text = "₹"+property?.rate.toString()

                    if(property?.rate === null) {
                        btnPayment.isEnabled = false
                        txtFee.text = "No Fee Data!"
                    }



                    MixpanelManager.track("Property Details", property)
                } else {
                    val errorMessage = response.errorBody()?.string();
                    if(errorMessage!!.contains("Property not found for the provided QR code")) {
                        Toast.makeText(this@PropertyDetailsActivity, "Property not found.", Toast.LENGTH_LONG).show();
                        val intent = Intent(this@PropertyDetailsActivity, HomeActivity::class.java)
                        startActivity(intent)
                    }
                    Log.d("PropertyDetailsActivity", "Error: ${response.errorBody()?.string()}")
                    Log.d("PropertyDetailsActivity", "Response: ${response.body()}")
                    MixpanelManager.track("Property Result Failure", response.body())

                }
            }

            override fun onFailure(call: Call<PropertyResponse>, t: Throwable) {
                MixpanelManager.trackError("Property Loading Failed", t)
                t.printStackTrace()
            }
        })





        btnPayment.setOnClickListener {
            MixpanelManager.track("Payment Button Clicked")
            startPayment(property?.rate, "ASRO-${System.currentTimeMillis()}")

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

//        val paymentMode = data?.getStringExtra("paymentMode")
//            ?: data?.getStringExtra("payment_type")
//            ?: data?.getStringExtra("mode")
//            ?: data?.getStringExtra("txnType")
//            ?: "UNKNOWN"



        val response = data?.getStringExtra("response")
        Log.d("PropertyDetailsActivity", "Response: $response")

        val paymentMode = try {
            val response = data?.getStringExtra("response")
            if (response != null) {
                val json = JSONObject(response)
                json.getJSONObject("result")
                    .getJSONObject("txn")
                    .optString("paymentMode", "UNKNOWN")
            } else {
                "UNKNOWN"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "UNKNOWN"
        }
        Log.d("PropertyDetailsActivity", "Payment Mode: $paymentMode")

        paymentResult.put("requestCode", requestCode)
        paymentResult.put("resultCode", resultCode)
        paymentResult.put("transactionId", transactionId)
        paymentResult.put("status", status)

        MixpanelManager.track("Payment Result", JSONObject(response))

        Log.d("PropertyDetailsActivity", "Request Code: $requestCode == $PaymentLauncher.REQUEST_CODE_PAYMENT")
        Log.d("PropertyDetailsActivity", "Result Code: $resultCode == $RESULT_OK")

        if (requestCode == PaymentLauncher.REQUEST_CODE_PAYMENT) {

            MixpanelManager.track("Payment Mode $paymentMode")

            if (resultCode == RESULT_OK) {
                MixpanelManager.track("Payment Success", paymentResult)

                Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show()

                lifecycleScope.launch {
                    var lat: Double = 0.0
                    var lng: Double = 0.0

                    val location = locationHelper.getCurrentLocationSuspend()
                    if (location != null) {
                        lat = location.latitude
                        lng = location.longitude
                    }

//                val transactionId = data?.getStringExtra("transactionId")
//
//                val status = data?.getStringExtra("status")

                    val calendar = Calendar.getInstance()
                    val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based
                    val year = calendar.get(Calendar.YEAR)

                    val request = DirectCollection(
                        propertyId = property?._id.toString(),
                        amountPaid = property?.rate,
                        billAmount = property?.rate,
                        paymentType = paymentMode.toLowerCase(),
                        paymentStatus= "success",
                        collectorId = SessionManager.getUserId(),
                        collectionPeriod = CollectionPeriod(
                            month = month,
                            year = year
                        ),
                        remark = "",
                        location = PropertyLocation(
                            latitude = lat,
                            longitude = lng
                        )
                    )
                    val billsService = ApiClient.retrofit.create(BillService::class.java)
                    billsService.createDirectCollection(request)
                        .enqueue(object : Callback<ApiResponse> {

                            override fun onResponse(
                                call: Call<ApiResponse>,
                                response: Response<ApiResponse>
                            ) {
                                if (response.isSuccessful) {
                                    val body = response.body()
                                    println("Success: ${body?.message}")
                                } else {
                                    println("Error: ${response.errorBody()?.string()}")
                                }
                            }

                            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                                t.printStackTrace()
                            }
                        })


                }
                EzeAPI.printReceipt(this, REQUEST_CODE_PRINT_RECEIPT, transactionId);
                // TODO: navigate to print screen here.
                navigateToHome()
            } else {
                MixpanelManager.track("Payment Failed", paymentResult)
            }

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted → call location again
            locationHelper.getCurrentLocation { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    hasLocation = true;
                    btnPayment.isEnabled = hasLocation
//                    resetMessage()
                    Log.d("PropertyDetailsActivity","onRequestPermissionsResult::Lat: $lat, Lng: $lng")
                } else {
                    println("Unable to fetch location")
                    Toast.makeText(this, "Cannot fetch location!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

//    fun resetMessage() {
//        if (hasLocation) {
//            txtMessage.setText("")
//        } else {
//            txtMessage.setText(getString(R.string.please_enable_location_to_continue))
//        }
//    }

    fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
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