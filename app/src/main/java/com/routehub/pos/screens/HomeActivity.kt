package com.routehub.pos.screens

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.routehub.pos.R
import com.routehub.pos.fragments.CollectionFragment
import com.routehub.pos.fragments.settings.SettingsFragment
import com.routehub.pos.helpers.LocaleHelper
import com.eze.api.EzeAPI
import com.routehub.pos.network.NetworkMonitor
import com.routehub.pos.network.NetworkState
import com.routehub.pos.network.NetworkStateAware
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private val REQUEST_CODE_INITIALIZE = 10001

    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var networkRibbon: TextView

    var currentNetworkState: NetworkState = NetworkState.OFFLINE

    fun initializePOS() {

        try {

            val jsonRequest = JSONObject()

//            jsonRequest.put("demoAppKey", "8901c766-f261-4631-ad8b-6771de798728")
            jsonRequest.put("demoAppKey", "e7b363a7-9a1a-4820-8667-a23ff686a5ea")
            jsonRequest.put("prodAppKey", "e7b363a7-9a1a-4820-8667-a23ff686a5ea")

            jsonRequest.put("merchantName", "ASR_SMARTCITY_PVT_LTD")
//            jsonRequest.put("userName", "5600430241")
            jsonRequest.put("userName", "5656541111")

            jsonRequest.put("currencyCode", "INR")

            // Demo or PROD
            jsonRequest.put("appMode", "PROD")

            jsonRequest.put("captureSignature", "true")
            jsonRequest.put("prepareDevice", "false")
            jsonRequest.put("captureReceipt", "true")

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

        networkRibbon = findViewById(R.id.networkRibbon)

        networkMonitor = NetworkMonitor(this) { state ->
            updateRibbon(state)
        }

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

    override fun onStart() {
        super.onStart()
        networkMonitor.start()
        Log.d("HomeActivity", "onStart: Network monitor started")
    }

    override fun onStop() {
        super.onStop()
        networkMonitor.stop()
        Log.d("HomeActivity", "onStop: Network monitor stopped")
    }

    private fun updateRibbon(state: NetworkState) {
        currentNetworkState = state

        when (state) {
            NetworkState.STABLE -> {
                networkRibbon.visibility = View.GONE
            }
            NetworkState.UNSTABLE -> {
                networkRibbon.visibility = View.VISIBLE
                networkRibbon.text = getString(R.string.network_unstable) // "Weak network — transactions may be delayed"
                networkRibbon.setBackgroundColor(Color.parseColor("#F59E0B")) // amber
            }
            NetworkState.OFFLINE -> {
                networkRibbon.visibility = View.VISIBLE
                networkRibbon.text = getString(R.string.network_offline) // "No network — new transactions blocked"
                networkRibbon.setBackgroundColor(Color.parseColor("#DC2626")) // red
            }
        }

        // Notify the currently visible fragment so it can disable
        // its "New Transaction" CTA, e.g. via an interface or a
        // shared ViewModel both Activity and Fragment observe.
        (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? NetworkStateAware)
            ?.onNetworkStateChanged(state)
    }
}