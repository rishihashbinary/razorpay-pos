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
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.routehub.pos.R
import com.routehub.pos.fragments.CollectionFragment
import com.routehub.pos.fragments.settings.SettingsFragment
import com.routehub.pos.helpers.LocaleHelper
import com.eze.api.EzeAPI
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.routehub.pos.network.NetworkMonitor
import com.routehub.pos.network.NetworkState
import com.routehub.pos.network.NetworkStateAware
import com.routehub.pos.onboarding.OnboardingTargetProvider
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private val REQUEST_CODE_INITIALIZE = 10001

    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var networkRibbon: TextView
    private lateinit var bottomNavigation: BottomNavigationView

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

        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        loadFragment(CollectionFragment())

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_collection -> loadFragment(CollectionFragment())

                R.id.nav_settings -> loadFragment(SettingsFragment())

            }

            true
        }
        // Wait for the fragment's view to actually be laid out before
        // attempting to read view bounds for the tap targets.
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    fragment: Fragment,
                    v: View,
                    savedInstanceState: Bundle?
                ) {
                    if (fragment is CollectionFragment) {
                        v.post { maybeShowOnboarding(fragment) }
                        // Only need this once
                        fm.unregisterFragmentLifecycleCallbacks(this)
                    }
                }
            },
            false
        )
    }

    private fun maybeShowOnboarding(fragment: CollectionFragment) {
        val prefs = getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEN_FEE_COLLECTION_ONBOARDING, false)) return

        showOnboarding(fragment)
    }

    private fun showOnboarding(fragment: OnboardingTargetProvider) {

        val tabCollection = bottomNavigation.findViewById<View>(R.id.nav_collection)
        val tabSettings = bottomNavigation.findViewById<View>(R.id.nav_settings)

        val scanQr = fragment.getScanQrView()
        val mobileNumber = fragment.getMobileNumberView()
        val addManually = fragment.getAddManuallyView()

        // Guard: if any target view isn't available yet, skip rather than crash
        if (scanQr == null || mobileNumber == null || addManually == null) {
            Log.w("Onboarding", "Target views not ready, skipping onboarding")
            return
        }

        TapTargetSequence(this)
            .targets(
                TapTarget.forView(
                    tabCollection,
                    "Collection Tab",
                    getString(R.string.scan_or_search_properties_here_to_collect_fees)
                ).cancelable(false)
                    .transparentTarget(true),

                TapTarget.forView(
                    tabSettings,
                    "Settings Tab",
                    getString(R.string.settings_onboarding)
                ).cancelable(false)
                    .transparentTarget(true),

                TapTarget.forView(
                    scanQr,
                    getString(R.string.scan_qr),
                    getString(R.string.scan_qr_onboarding)
                ).cancelable(false),

                TapTarget.forView(
                    mobileNumber,
                    getString(R.string.mobile_number),
                    getString(R.string.search_property_onboarding)
                ).cancelable(false),

                TapTarget.forView(
                    addManually,
                    getString(R.string.add_manually),
                    getString(R.string.add_property_onboarding)
                ).cancelable(false)
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    markOnboardingSeen()
                }
                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    markOnboardingSeen()
                }
            })
            .start()
    }

    private fun markOnboardingSeen() {
        getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SEEN_FEE_COLLECTION_ONBOARDING, true)
            .apply()
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

    companion object {
        private const val PREF_ONBOARDING = "onboarding"
        private const val KEY_SEEN_FEE_COLLECTION_ONBOARDING = "seen_fee_collection_onboarding"
    }
}