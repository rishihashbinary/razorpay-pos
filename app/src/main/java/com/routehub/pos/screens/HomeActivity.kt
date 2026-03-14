package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.routehub.pos.R
import com.routehub.pos.fragments.CollectionFragment
import com.routehub.pos.fragments.SettingsFragment
import com.routehub.pos.helpers.LocaleHelper
import com.eze.api.EzeAPI
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private val REQUEST_CODE_INITIALIZE = 10001

    fun initializePOS() {

        try {

            val jsonRequest = JSONObject()

            jsonRequest.put("demoAppKey", "YOUR_DEMO_APP_KEY")
            jsonRequest.put("prodAppKey", "YOUR_PROD_APP_KEY")

            jsonRequest.put("merchantName", "YOUR_MERCHANT_NAME")
            jsonRequest.put("userName", "POS_USER_01")

            jsonRequest.put("currencyCode", "INR")

            // Demo or PROD
            jsonRequest.put("appMode", "DEMO")

            jsonRequest.put("captureSignature", "true")
            jsonRequest.put("prepareDevice", "false")
            jsonRequest.put("captureReceipt", "false")

            EzeAPI.initialize(this, REQUEST_CODE_INITIALIZE, jsonRequest)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        LocaleHelper.loadLocale(this)

        super.onCreate(savedInstanceState)

        initializePOS()

        setContentView(R.layout.activity_home)

        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        loadFragment(CollectionFragment())

        nav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_collection -> loadFragment(CollectionFragment())

                R.id.nav_settings -> loadFragment(SettingsFragment())

            }

            true
        }
    }

    private fun loadFragment(fragment: Fragment) {

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_INITIALIZE) {

            val responseString = data?.getStringExtra("response") ?: return
            val response = JSONObject(responseString)

            if (resultCode == RESULT_OK) {

                val result = response.getJSONObject("result")

                Log.d("POS_INIT", "Initialization Success")

                Toast.makeText(this, "POS Ready", Toast.LENGTH_LONG).show()

            } else {

                val error = response.getJSONObject("error")

                val message = error.getString("message")

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}