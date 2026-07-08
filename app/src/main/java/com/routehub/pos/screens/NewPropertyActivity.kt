package com.routehub.pos.screens

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.routehub.pos.R
import com.routehub.pos.analytics.MixpanelManager
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.helpers.DropdownHelper
import com.routehub.pos.helpers.LocationHelper
import com.routehub.pos.helpers.QrHelper
import com.routehub.pos.models.NewProperty
import com.routehub.pos.models.PropertyCategory
import com.routehub.pos.models.PropertyType
import com.routehub.pos.models.PropertyUsageType
import com.routehub.pos.models.Ward
import com.routehub.pos.models.Zone
import com.routehub.pos.models.responses.ApiListResponse
import com.routehub.pos.models.responses.ApiObjectResponse
import com.routehub.pos.persistence.MasterDataPrefs
import com.routehub.pos.services.PropertiesService
import com.routehub.pos.services.WardService
import com.routehub.pos.services.ZoneService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewPropertyActivity : AppCompatActivity() {

    private val propertyService = ApiClient.retrofit.create(PropertiesService::class.java)
    private val zoneService = ApiClient.retrofit.create(ZoneService::class.java)
    private val wardService = ApiClient.retrofit.create(WardService::class.java)


    private lateinit var zoneDrop: DropdownHelper<Zone>
    private lateinit var wardDrop: DropdownHelper<Ward>
    private lateinit var typeDrop: DropdownHelper<PropertyType>
    private lateinit var categoryDrop: DropdownHelper<PropertyCategory>
    private lateinit var usageDrop: DropdownHelper<PropertyUsageType>

    private var allZones: List<Zone> = listOf<Zone>()
    private var allWards: List<Ward> = listOf<Ward>()
    private var allTypes: List<PropertyType>? = listOf<PropertyType>()
    private var allCategories: List<PropertyCategory>? = listOf<PropertyCategory>()
    private var allUsageTypes: List<PropertyUsageType>? = listOf<PropertyUsageType>()

    private var qrCode: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        locationHelper = LocationHelper(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_property)

        setupDropdowns()

        setupActions()

        loadData()

        loadLocation()


    }

    fun loadLocation() {
        //         Step 1: Check permission
        if (!locationHelper.hasLocationPermission()) {
            Toast.makeText(this, "No location permission found, requesting.", Toast.LENGTH_LONG).show();
            locationHelper.requestPermission(this)
        } else {
            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
            // Step 2: Get location
            locationHelper.getCurrentLocation { location ->
                if (location != null) {

                    latitude = location.latitude
                    longitude = location.longitude
                    Toast.makeText(this, "Got Location! [$latitude, $longitude]", Toast.LENGTH_LONG).show();

                    if(latitude != 0.0) {
                        findViewById<TextView>(R.id.tvLocation).text = "$latitude, $longitude"
                        ImageViewCompat.setImageTintList(
                            findViewById<ImageView>(R.id.ivLocation),
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_500)))
                    } else {
                        ImageViewCompat.setImageTintList(
                            findViewById<ImageView>(R.id.ivLocation),
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_500)))
                    }

                } else {
                    Toast.makeText(this, "Cannot fetch location!", Toast.LENGTH_LONG).show();
                    println("Unable to fetch location")
                    ImageViewCompat.setImageTintList(
                        findViewById<ImageView>(R.id.ivLocation),
                        ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_500)))
                }
            }
        }
    }

    fun setDropdownHint(viewId: Int, hint: String) {
        val layout = findViewById<TextInputLayout>(viewId)
        layout.hint = hint
    }

    // ---------------------------
    // 🔽 Setup Dropdowns
    // ---------------------------
    private fun setupDropdowns() {
        val zoneView = findViewById<View>(R.id.dropZone)
        val zoneAuto = zoneView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        zoneDrop = DropdownHelper<Zone>(zoneAuto) { it.name }
        setDropdownHint(R.id.dropZone, "Select Zone")


        val wardView = findViewById<View>(R.id.dropWard)
        val wardAuto = wardView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        wardDrop = DropdownHelper(wardAuto) { it.number }
        setDropdownHint(R.id.dropWard, "Select Ward")

        val  propertyTypeView = findViewById<View>(R.id.dropPropertyType)
        val propertyTypeAuto = propertyTypeView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        typeDrop = DropdownHelper(propertyTypeAuto) { it.typeName }
        setDropdownHint(R.id.dropPropertyType, "Select Property Type")

        val categoryView = findViewById<View>(R.id.dropCategory)
        val categoryAuto = categoryView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        categoryDrop = DropdownHelper(categoryAuto) { it.categoryName }
        setDropdownHint(R.id.dropCategory, "Select Property Category")

        val usageView = findViewById<View>(R.id.dropUsageType)
        val usageAuto = usageView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        usageDrop = DropdownHelper(usageAuto) { it.typeName }
    }

    // ---------------------------
    // 🔽 Load API Data
    // ---------------------------
    private fun loadData() {

        allZones = MasterDataPrefs.getZones(this)
        allWards = MasterDataPrefs.getWards(this)
        allTypes = MasterDataPrefs.getTypes(this)
        allCategories = MasterDataPrefs.getCategories(this)
        allUsageTypes = MasterDataPrefs.getUsage(this)

        if (allZones.isEmpty()) {
            toast("App data not loaded. Please login again.")
            return
        }

        zoneDrop.setItems(allZones)
        typeDrop.setItems(allTypes ?: emptyList())

        setupDependencies()
    }

    // ---------------------------
    // 🔁 Dependency Logic
    // ---------------------------
    private fun setupDependencies() {

        // Zone → Ward
        zoneDrop.setOnItemSelected { selectedZone, pos ->

            val filtered = allWards?.filter { it.zoneId._id == selectedZone._id }

            Log.d("NewPropertyActivity", "Selected Zone: $selectedZone")
            Log.d("NewPropertyActivity", "Filtered Wards: ${filtered?.size}")

            wardDrop.setItems(filtered ?: emptyList())
            wardDrop.clear()
        }

        // Type → Category
        typeDrop.setOnItemSelected { selectedType, pos ->

                val selectedType = allTypes?.get(pos)
                val filtered = allCategories?.filter { it.propertyTypeId == selectedType?._id }

                categoryDrop.setItems(filtered ?: emptyList())
                categoryDrop.clear()
                usageDrop.clear()
            }

        // Category → Usage
        categoryDrop
            .setOnItemSelected { selectedCat, pos ->

//                val selectedCat = categoryDrop.selectedItem ?: return@setOnItemClickListener

                Log.d("NewPropertyActivity", "Selected Category: ${selectedCat.categoryName}")

                Log.d("NewPropertyActivity", "Usage Sample: ${allUsageTypes?.get(0)}")
                val filtered = allUsageTypes?.filter {
                    it.propertyCategoryId == selectedCat._id
                }

                Log.d("NewPropertyActivity", "Filtered Usage: ${filtered?.size}")

                usageDrop.setItems(filtered ?: emptyList())
                usageDrop.clear()
            }
    }

    // ---------------------------
    // 🎯 Actions
    // ---------------------------
    private fun setupActions() {

        findViewById<MaterialCardView>(R.id.cardQr).setOnClickListener {
            startScanner()
        }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            submit()
        }
    }

    // ---------------------------
    // ✅ Submit
    // ---------------------------
    private fun submit() {

        val name = findViewById<TextInputEditText>(R.id.etName).text.toString()
        val mobile = findViewById<TextInputEditText>(R.id.etMobile).text.toString()
        val address = findViewById<TextInputEditText>(R.id.etAddress).text.toString()

        if (name.length < 3) return toast("Invalid Name")

        if (!mobile.matches(Regex("^[6-9]\\d{9}$")))
            return toast("Invalid Mobile")

        if (address.isBlank()) return toast("Address required")

        val zone = zoneDrop.selectedItem ?: return toast("Select Zone")
        val ward = wardDrop.selectedItem ?: return toast("Select Ward")
        val type = typeDrop.selectedItem ?: return toast("Select Type")
        val category = categoryDrop.selectedItem ?: return toast("Select Category")
        val usage = usageDrop.selectedItem ?: return toast("Select Usage")

        if (qrCode.isBlank()) return toast("Scan QR")

        val request = NewProperty(
            address1 = address,
            lat = latitude,
            lon = longitude,
            mobileNo = mobile,
            name = name,
            propertyCategoryId = category._id,
            propertyTypeId = type._id,
            propertyUsageTypeId = usage._id,
            qrCode = qrCode,
            wardId = ward._id,
            zoneId = zone._id,
        )

        MixpanelManager.track("New Property", request)

            try {
                showLoader(true)

                propertyService.createProperty(request).enqueue(
                    object : Callback<ApiObjectResponse<Any>> {
                        override fun onResponse(
                            call: Call<ApiObjectResponse<Any>>,
                            response: Response<ApiObjectResponse<Any>>
                        ) {
                            if (response.isSuccessful) {
                                toast("✅ Property saved successfully.")
                                MixpanelManager.track("Property created successfully", request)
                                clearForm()
                            } else {
                                val errorString = response.errorBody()?.string()
                                Log.e("NewPropertyActivity", "NOT isSuccessful")
                                Log.d("NewPropertyActivity", "Error: ${errorString}")
                                val errorMessage = Gson().fromJson(errorString, ApiListResponse::class.java)
//                                if(response.errorBody()?.toString()?.contains("QR Code") == true) {
//                                    toast("❌ QR code already assigned to another property.")
//                                } else {
                                    toast("❌ ${errorMessage?.message}")
//                                }

                            }
                        }

                        override fun onFailure(
                            call: Call<ApiObjectResponse<Any>?>,
                            t: Throwable
                        ) {
                            toast("Save failed ❌")
                            Log.e("NewPropertyActivity", "onFailure")
                            t.printStackTrace()
                            MixpanelManager.track("Property creation failed")
                        }
                    }
                )

            } catch (e: Exception) {
                e.printStackTrace()
                toast("Save failed ❌")
            } finally {
                showLoader(false)
            }
    }

    // ---------------------------
    // 🧹 Clear Form
    // ---------------------------
    private fun clearForm() {
        findViewById<TextInputEditText>(R.id.etName).setText("")
        findViewById<TextInputEditText>(R.id.etMobile).setText("")
        findViewById<TextInputEditText>(R.id.etAddress).setText("")

        zoneDrop.clear()
        wardDrop.clear()
        typeDrop.clear()
        categoryDrop.clear()
        usageDrop.clear()

        qrCode = ""
        findViewById<TextView>(R.id.tvQr).text = "No QR Selected"
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showLoader(show: Boolean) {
        // Optional: ProgressBar visibility
    }

    private fun startScanner() {

        val integrator = IntentIntegrator(this)

        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan Property QR Code")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)

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

                Log.d("NewPropertyActivity", "QR Code: $qrCode")

                if(qrCode != null) {

                    findViewById<TextView>(R.id.tvQr).text = qrCode
                    this.qrCode = qrCode


                } else {
                    toast("Invalid QR code.")
                    Log.d("NewPropertyActivity", "Invalid QR code")
                }

//                finish()

            } else {

//                finish()

            }

        } else {

            super.onActivityResult(requestCode, resultCode, data)

        }
    }
}