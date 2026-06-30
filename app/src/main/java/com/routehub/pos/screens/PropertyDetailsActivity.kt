package com.routehub.pos.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.eze.api.EzeAPI
import com.google.gson.Gson
import com.routehub.pos.PrintCallback
import com.routehub.pos.R
import com.routehub.pos.analytics.AnalyticsTracker
import com.routehub.pos.analytics.MixpanelManager
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.clients.SessionManager
import com.routehub.pos.helpers.DateHelper
import com.routehub.pos.helpers.LocationHelper
import com.routehub.pos.helpers.PlayHelper
import com.routehub.pos.helpers.ReceiptPrintHelper
import com.routehub.pos.models.CollectionPeriod
import com.routehub.pos.models.DirectCollection
import com.routehub.pos.models.DueItem
import com.routehub.pos.models.Property
import com.routehub.pos.models.PropertyLocation
import com.routehub.pos.models.Reason
import com.routehub.pos.models.ReceiptData
import com.routehub.pos.models.razorpay.TransactionResponse
import com.routehub.pos.models.responses.ApiListResponse
import com.routehub.pos.models.responses.ApiObjectResponse
import com.routehub.pos.models.responses.BillData
import com.routehub.pos.models.responses.PropertyResponse
import com.routehub.pos.payments.PaymentLauncher
import com.routehub.pos.screens.dues.DueSelectionActivity
import com.routehub.pos.screens.payment.PaymentFailureBottomSheet
import com.routehub.pos.services.BillService
import com.routehub.pos.services.PropertiesService
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Date

class PropertyDetailsActivity : AppCompatActivity() {

    var property: Property? = null
    var hasLocation: Boolean = false;
    private lateinit var locationHelper: LocationHelper
    lateinit var btnPayment: Button
    lateinit var txtMessage: TextView
    lateinit var txtFee: TextView

    val apiService = ApiClient.retrofit.create(PropertiesService::class.java)
    val billsService = ApiClient.retrofit.create(BillService::class.java)

    private val REQUEST_CODE_PAY = 10016
    private val REQUEST_CODE_PRINT_RECEIPT = 10028
//    private var googleMap: GoogleMap? = null

    var reasons: List<Reason>? = null;

    private var dueList: List<DueItem> = emptyList()


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
        val propertyDetails = intent.getStringExtra("propertyDetails")

        val props = JSONObject()
        props.put("qrCode", qrCode)
        if(qrCode !== null) {
            MixpanelManager.track("Loading Property Using QR", props)
        } else {
            MixpanelManager.track("Loading Property Using JSON", propertyDetails)
        }

        locationHelper = LocationHelper(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
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
        txtFee = findViewById<TextView>(R.id.txtFee)
        txtMessage = findViewById<TextView>(R.id.txtMessage)
        btnPayment = findViewById<Button>(R.id.btnPayment)

        //         Step 1: Check permission
        if (!locationHelper.hasLocationPermission()) {
            Toast.makeText(this, "No location permission found, requesting.", Toast.LENGTH_LONG).show();
            locationHelper.requestPermission(this)
        } else {
//            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
            // Step 2: Get location
            locationHelper.getCurrentLocation { location ->
                if (location != null) {
//                    Toast.makeText(this, "Got Location!", Toast.LENGTH_LONG).show();
                    val lat = location.latitude
                    val lng = location.longitude
                    hasLocation = true;
                    resetMessage()
                    btnPayment.isEnabled = hasLocation
                    println("Lat: $lat, Lng: $lng")
                } else {
                    Toast.makeText(this, "Cannot fetch location!", Toast.LENGTH_LONG).show();
                    println("Unable to fetch location")
                }
            }
        }

        if(!hasLocation) {
            txtMessage.setText(getString(R.string.please_enable_location_to_continue))
        }

        btnPayment.isEnabled = hasLocation

        if(qrCode !== null) {
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

                        txtFee.text = "₹" + property?.rate.toString()

                        if (property?.rate === null) {
                            btnPayment.isEnabled = false
                            txtFee.text = "No Fee Data!"
                        }



                        MixpanelManager.track("Property Details", property)
                    } else {
                        val errorMessage = response.errorBody()?.string();
                        if (errorMessage!!.contains("Property not found for the provided QR code")) {
                            Toast.makeText(
                                this@PropertyDetailsActivity,
                                "Property not found.",
                                Toast.LENGTH_LONG
                            ).show();
                            val intent =
                                Intent(this@PropertyDetailsActivity, HomeActivity::class.java)
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
        } else {
            property = Gson().fromJson(propertyDetails, Property::class.java)

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

            txtFee.text = "₹" + property?.rate.toString()

            if (property?.rate === null) {
                btnPayment.isEnabled = false
                txtFee.text = "No Fee Data!"
            } else {
                txtFee.setOnClickListener {
                    MixpanelManager.track("Due Breakdown Clicked")
                    val intent = Intent(this, DueSelectionActivity::class.java)
                    intent.putParcelableArrayListExtra(
                        DueSelectionActivity.EXTRA_DUES,
                        ArrayList(dueList)
                    )

                    dueLauncher.launch(intent)
                }
            }
        }






        btnPayment.setOnClickListener {
            MixpanelManager.track("Payment Button Clicked")
            startPayment(property?.rate, "ASRO-${System.currentTimeMillis()}")

//            PaymentLauncher.startPayment(
//                this,
//                1
//            )
        }

        val btnReject = findViewById<Button>(R.id.btnRejectPayment)

        btnReject.setOnClickListener {
            val bottomSheet = PaymentFailureBottomSheet { reason, remarks ->

                MixpanelManager.track(
                    "Payment Rejected", mapOf(
                        "reason" to reason,
                        "remarks" to remarks
                    )
                )

                Toast.makeText(this, "Captured: ${reason}", Toast.LENGTH_SHORT).show()

                lifecycleScope.launch {
                    var lat: Double = 0.0
                    var lng: Double = 0.0

                    val location = locationHelper.getCurrentLocationSuspend()
                    if (location != null) {
                        lat = location.latitude
                        lng = location.longitude
                    }

                    val calendar = Calendar.getInstance()
                    val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based
                    val year = calendar.get(Calendar.YEAR)

                    val denialDetails = DirectCollection(
                        propertyId = property?._id, amountPaid = 0F, billAmount = property?.rate,
                        collectorId = SessionManager.getUserId(),
                        collectionPeriod = CollectionPeriod(
                            month = month,
                            year = year
                        ),
                        location = PropertyLocation(
                            latitude = lat,
                            longitude = lng
                        ),
                        paymentStatus = "denied",
                        denialReason = reason,
                        remark = remarks
                    )

                    billsService.denyPayment(denialDetails).enqueue(object : Callback<ApiListResponse<Any>> {

                        override fun onResponse(
                            call: Call<ApiListResponse<Any>>,
                            response: Response<ApiListResponse<Any>>
                        ) {
                            if (response.isSuccessful) {
                                val body = response.body()
                                println("Success: ${body?.message}")
                            } else {
                                println("Error: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<ApiListResponse<Any>>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
                }
            }

            bottomSheet.show(supportFragmentManager, "PaymentFailure")
        }
        fetchBills()
    }

    override fun onResume() {
        super.onResume()
        Log.d("PropertyDetailsActivity", "onResume: Checking location")
        locationHelper.getCurrentLocation { location ->
            if (location != null) {
//                    Toast.makeText(this, "Got Location!", Toast.LENGTH_LONG).show();
                val lat = location.latitude
                val lng = location.longitude
                hasLocation = true;
                resetMessage()
                btnPayment.isEnabled = hasLocation
                Log.d("PropertyDetailsActivity", "onResume: Lat: $lat, Lng: $lng")
            } else {
                Toast.makeText(this, "Cannot fetch location!", Toast.LENGTH_LONG).show();
                println("Unable to fetch location")
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)
        val paymentResult = JSONObject()

        val status = data?.getStringExtra("status")

        val response = data?.getStringExtra("response")
        Log.d("PropertyDetailsActivity", "Response: $response")

        val result = try {
            val response = data?.getStringExtra("response")

            if (response != null) {
                val parsed = Gson().fromJson(response, TransactionResponse::class.java)
                parsed.result
            } else {
                null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        val txn = result?.txn
        val receipt = result?.receipt
        val customer = result?.customer
        val references = result?.references

        Log.d("PropertyDetailsActivity", "Payment Mode: $txn.paymentMode")
        Log.d("PropertyDetailsActivity", "Transaction Id: $txn.txnId")

        paymentResult.put("requestCode", requestCode)
        paymentResult.put("resultCode", resultCode)
        paymentResult.put("transactionId", txn?.txnId)
        paymentResult.put("status", status)

        MixpanelManager.track("Payment Result", response)

        Log.d("PropertyDetailsActivity", "Request Code: $requestCode == $PaymentLauncher.REQUEST_CODE_PAYMENT")
        Log.d("PropertyDetailsActivity", "Result Code: $resultCode == $RESULT_OK")

        if (requestCode == PaymentLauncher.REQUEST_CODE_PAYMENT) {

//            MixpanelManager.track("Payment Mode $paymentMode")

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

                    val calendar = Calendar.getInstance()
                    val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based
                    val year = calendar.get(Calendar.YEAR)

                    val request = DirectCollection(
                        propertyId = property?._id.toString(),
                        amountPaid = property?.rate,
                        billAmount = property?.rate,
                        paymentType = txn?.paymentMode?.toLowerCase(),
                        paymentStatus = "success",
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

                    billsService.createDirectCollection(request)
                        .enqueue(object : Callback<ApiListResponse<Any>> {

                            override fun onResponse(
                                call: Call<ApiListResponse<Any>>,
                                response: Response<ApiListResponse<Any>>
                            ) {
                                if (response.isSuccessful) {
                                    val body = response.body()
                                    println("Success: ${body?.message}")
                                } else {
                                    println("Error: ${response.errorBody()?.string()}")
                                }
                            }

                            override fun onFailure(call: Call<ApiListResponse<Any>>, t: Throwable) {
                                t.printStackTrace()
                            }
                        })


                }
                val receiptDate = DateHelper.getReadableDate(receipt?.receiptDate)
                Log.d(
                    "PropertyDetailsActivity", "Receipt Date $receiptDate")
//                EzeAPI.printReceipt(this, REQUEST_CODE_PRINT_RECEIPT, transactionId);
                // TODO: navigate to print screen here.
                val receiptData = ReceiptData(
                    merchantName = "ASR Smart City Pvt. Ltd.",
                    txnId = txn?.txnId,
                    paymentMode = txn?.paymentMode,
                    reference1 = references?.reference1,
                    status = "Success",
                    amount = property?.rate,
                    usageType = property?.propertyUsageTypeId?.typeName,
                    customerName = property?.name ?: property?.ownerName ?: property?.address1,
                    customerPhone = property?.mobileNo,
                    receiptDate = DateHelper.getReadableDate(receipt?.receiptDate)
                )
//                MixpanelManager.track("Initiating Print.", receiptData)
                ReceiptPrintHelper.printReceipt(this, receiptData, object : PrintCallback {
                    override fun onSuccess() {
                        MixpanelManager.track("Print Success")
                        navigateToHome()
                    }

                    override fun onError(error: String?) {
                        val props = JSONObject()
                        props.put("errorMessage", error ?: "Unknown Error")
                        MixpanelManager.track("Print Failed", props)
                    }

                });
//                MixpanelManager.track("Sent to Printer")
//                navigateToHome()
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
                    resetMessage()
                    Log.d("PropertyDetailsActivity","onRequestPermissionsResult::Lat: $lat, Lng: $lng")
                } else {
                    println("Unable to fetch location")
                    Toast.makeText(this, "Cannot fetch location!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    fun resetMessage() {
        if (hasLocation) {
            txtMessage.setText("")
        } else {
            txtMessage.setText(getString(R.string.please_enable_location_to_continue))
        }
    }

    fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private val dueLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data

            val selectedDues =
                data?.getParcelableArrayListExtra<DueItem>(
                    DueSelectionActivity.RESULT_SELECTED_DUES
                ) ?: emptyList()

            val total =
                data?.getDoubleExtra(
                    DueSelectionActivity.RESULT_TOTAL,
                    0.0
                ) ?: 0.0

            Log.d("PropertyDetailsActivity", "Selected Dues: $total")

            txtFee.text = "₹$total"
        }
    }

    private fun fetchBills() {
        billsService.getPropertyBills(property?._id, 1, 50).enqueue(object : Callback<ApiObjectResponse<BillData>> {
            override fun onResponse(
                call: Call<ApiObjectResponse<BillData>>,
                response: Response<ApiObjectResponse<BillData>>
            ) {
                if (response.isSuccessful) {
                    val bills = response.body()?.data?.bills ?: emptyList()
//                    println("Success: ${body?.data}")
                    // ✅ Convert to DueItems (only pending)
                    val dueItems = bills
//                        .filter { it.status == "PENDING" } // adjust based on API
                        .map {
                            DueItem(
                                id = it._id ?: Date().time.toString(),
                                monthLabel = "${it.billPeriod.month} - ${it.billPeriod.year}",
                                amount = it.billAmount ?: 0.0,
                                isSelected = true
                            )
                        }

                    // ✅ Calculate total due
                    val totalDue = dueItems.sumOf { it.amount }

                    // 👉 Update UI on current screen
                    updateAmountDueUI(totalDue, dueItems.size)

                    // 👉 Store for later navigation
                    this@PropertyDetailsActivity.dueList = dueItems
                } else {
                    println("Error: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<ApiObjectResponse<BillData>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun updateAmountDueUI(total: Double, count: Int) {
        txtFee.text = "₹$total"
//        tvSubLabel.text = "$count months pending"
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