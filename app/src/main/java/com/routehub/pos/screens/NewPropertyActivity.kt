package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.integration.android.IntentIntegrator
import com.routehub.pos.R
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.helpers.DropdownHelper
import com.routehub.pos.helpers.QrHelper
import com.routehub.pos.models.NewProperty
import com.routehub.pos.models.PropertyCategory
import com.routehub.pos.models.PropertyType
import com.routehub.pos.models.PropertyUsageType
import com.routehub.pos.models.Ward
import com.routehub.pos.models.Zone
import com.routehub.pos.models.responses.ApiResponse
import com.routehub.pos.services.PropertiesService
import com.routehub.pos.services.WardService
import com.routehub.pos.services.ZoneService
import kotlinx.coroutines.launch
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_property)

        setupDropdowns()
        setupActions()

        loadData()
    }

    // ---------------------------
    // 🔽 Setup Dropdowns
    // ---------------------------
    private fun setupDropdowns() {
        val zoneView = findViewById<View>(R.id.dropZone)
        val zoneAuto = zoneView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        zoneDrop = DropdownHelper<Zone>(zoneAuto) { it.name }

        val wardView = findViewById<View>(R.id.dropWard)
        val wardAuto = wardView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        wardDrop = DropdownHelper(wardAuto) { it.number }

        val  propertyTypeView = findViewById<View>(R.id.dropPropertyType)
        val propertyTypeAuto = propertyTypeView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        typeDrop = DropdownHelper(propertyTypeAuto) { it.typeName }

        val categoryView = findViewById<View>(R.id.dropCategory)
        val categoryAuto = categoryView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        categoryDrop = DropdownHelper(categoryAuto) { it.categoryName }

        val usageView = findViewById<View>(R.id.dropUsageType)
        val usageAuto = usageView.findViewById<AutoCompleteTextView>(R.id.autoComplete)
        usageDrop = DropdownHelper(usageAuto) { it.typeName }
    }

    // ---------------------------
    // 🔽 Load API Data
    // ---------------------------
    private fun loadData() {

        lifecycleScope.launch {
            try {
                showLoader(true)

                zoneService.getZones().enqueue(object : Callback<ApiResponse<Zone>> {

                    override fun onResponse(
                        call: Call<ApiResponse<Zone>>,
                        response: Response<ApiResponse<Zone>>
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            allZones = body?.data!!
                            zoneDrop.setItems(allZones)
//                            println("Success: ${body?.message}")
                        } else {
                            println("Error: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Zone>>, t: Throwable) {
                        t.printStackTrace()
                    }
                })

                wardService.getWards().enqueue(object : Callback<ApiResponse<Ward>> {

                    override fun onResponse(
                        call: Call<ApiResponse<Ward>>,
                        response: Response<ApiResponse<Ward>>
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            allWards = body?.data!!
                            wardDrop.setItems(allWards)
//                            println("Success: ${body?.message}")
                        } else {
                            println("Error: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Ward>>, t: Throwable) {
                        t.printStackTrace()
                    }
                })

                setupDependencies()

            } catch (e: Exception) {
                e.printStackTrace()
                toast("Failed to load data ❌")
            } finally {
                showLoader(false)
            }
        }
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
//        findViewById<View>(R.id.dropPropertyType)
//            .findViewById<AutoCompleteTextView>(R.id.autoComplete)
//            .setOnItemClickListener { _, _, pos, _ ->
//
//                val selectedType = allTypes?.get(pos)
//                val filtered = allCategories?.filter { it.propertyTypeId == selectedType?._id }
//
//                categoryDrop.setItems(filtered)
//                categoryDrop.clear()
//                usageDrop.clear()
//            }
//
//        // Category → Usage
//        findViewById<View>(R.id.dropCategory)
//            .findViewById<AutoCompleteTextView>(R.id.autoComplete)
//            .setOnItemClickListener { _, _, pos, _ ->
//
//                val selectedCat = categoryDrop.selectedItem ?: return@setOnItemClickListener
//
//                val filtered = allUsageTypes?.filter {
//                    it.propertyCategoryId == selectedCat._id
//                }
//
//                usageDrop.setItems(filtered)
//                usageDrop.clear()
//            }
    }

    // ---------------------------
    // 🎯 Actions
    // ---------------------------
    private fun setupActions() {

        findViewById<Button>(R.id.btnScanQr).setOnClickListener {
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

        if (!mobile.matches(Regex("^(?:\\+91|91)?[6-9]\\d{9}$")))
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
            lat = 0.0,
            lon = 0.0,
            mobileNo = mobile,
            name = name,
            propertyCategoryId = category._id,
            propertyTypeId = type._id,
            propertyUsageTypeId = usage._id,
            qrCode = qrCode,
            wardId = ward._id,
            zoneId = zone._id,
        )

        lifecycleScope.launch {
            try {
                showLoader(true)

//                api.createProperty(request)

                toast("Property saved successfully ✅")
                clearForm()

            } catch (e: Exception) {
                toast("Save failed ❌")
            } finally {
                showLoader(false)
            }
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

                Log.d("NewPropertyActivity", "QR Code: $qrCode")

                if(qrCode != null) {

                    findViewById<TextView>(R.id.tvQr).text = qrCode

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