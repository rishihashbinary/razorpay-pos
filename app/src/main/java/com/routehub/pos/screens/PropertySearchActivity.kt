package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.routehub.pos.R
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.models.responses.PropertyResponse
import com.routehub.pos.services.PropertiesService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PropertySearchActivity : AppCompatActivity() {
    private lateinit var etMobile: EditText
    private lateinit var btnSearch: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_property_search)
        etMobile = findViewById(R.id.etMobile)
        btnSearch = findViewById(R.id.btnSearch)
        progressBar = findViewById(R.id.progressBar)

        btnSearch.setOnClickListener {
            val mobileNumber = etMobile.text.toString().trim()

            if (mobileNumber.length != 10) {
                Toast.makeText(this, "Enter valid mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            searchProperty(mobileNumber)
        }
    }

    private fun searchProperty(mobileNumber: String) {
        progressBar.visibility = View.VISIBLE

        val call = ApiClient.retrofit.create(PropertiesService::class.java).getPropertyByMobileNumber(mobileNumber)

        call.enqueue(object : Callback<PropertyResponse> {

            override fun onResponse(
                call: Call<PropertyResponse>,
                response: Response<PropertyResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {

                    val intent =
                        Intent(this@PropertySearchActivity, PropertyDetailsActivity::class.java)
                    intent.putExtra("mobileNumber", mobileNumber)
                    startActivity(intent)

                } else {
                    Toast.makeText(this@PropertySearchActivity, "Property not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PropertyResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@PropertySearchActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}