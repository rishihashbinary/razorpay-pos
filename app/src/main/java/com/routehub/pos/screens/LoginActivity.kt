package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.se.omapi.Session
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.routehub.pos.R
import com.routehub.pos.analytics.MixpanelManager
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.clients.SessionManager
import com.routehub.pos.models.LoginRequest
import com.routehub.pos.models.responses.LoginResponse
import com.routehub.pos.persistence.MasterDataPrefs
import com.routehub.pos.services.AuthService
import com.routehub.pos.services.PropertiesService
import com.routehub.pos.services.WardService
import com.routehub.pos.services.ZoneService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    val authService = ApiClient.retrofit.create(AuthService::class.java);
    private val propertyService = ApiClient.retrofit.create(PropertiesService::class.java)
    private val zoneService = ApiClient.retrofit.create(ZoneService::class.java)
    private val wardService = ApiClient.retrofit.create(WardService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "initializing MixPanel")
        MixpanelManager.initialize(this,  "33f0937bb8ec54f0a6b39d404275ffd1")

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        username.setText("asr.ucc")
        password.setText("Asr@1234")

        loginBtn.setOnClickListener {

//            val user = "admin" //username.text.toString()
//            val pass = "123" //password.text.toString()
            val request = LoginRequest(
                email = username.text.toString(),
                password = password.text.toString()
            )

            authService.login(true, false, "pos", request).enqueue(object :
                Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val token = response.body()?.data?.token
                        val userId = response.body()?.data?._id
                        val employeeId = response.body()?.data?.employeeId
                        Log.d("LoginActivity", "Token: $token")
                        ("Token: $token")
                        SessionManager.setToken(token);
                        SessionManager.setUserId(userId)
                        MixpanelManager.track("Login Success", request)
                        lifecycleScope.launch {
                            try {
                                preloadMasterData()
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            } catch (e: Exception) {
                                Toast.makeText(this@LoginActivity, "Failed to load app data", Toast.LENGTH_SHORT).show()
                                e.printStackTrace()
                            } finally {

                            }
                        }

                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    MixpanelManager.track("Login Failed", request)

                }
            })

//            if (user == "admin" && pass == "123") {
//                val props = JSONObject()
//                props.put("user", user)
//                MixpanelManager.track("Login Success", props)
//                startActivity(Intent(this, HomeActivity::class.java))
//            } else {
//                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
//            }
        }
    }

    private suspend fun preloadMasterData() = withContext(Dispatchers.IO) {

        val zonesRes = zoneService.getZones().execute()
        val wardsRes = wardService.getWards().execute()
        val typesRes = propertyService.getPropertyTypes().execute()
        val catRes = propertyService.getCategories().execute()
        val usageRes = propertyService.getUsageTypes().execute()

        val zones = zonesRes.body()?.data ?: emptyList()
        val wards = wardsRes.body()?.data ?: emptyList()
        val types = typesRes.body()?.data ?: emptyList()
        val categories = catRes.body()?.data ?: emptyList()
        val usage = usageRes.body()?.data ?: emptyList()

        Log.d("LoginActivity", "Zones: ${zones.size}")
        Log.d("LoginActivity", "Wards: ${wards.size}")
        Log.d("LoginActivity", "Types: ${types.size}")
        Log.d("LoginActivity", "Categories: ${categories.size}")
        Log.d("LoginActivity", "Usage: ${usage.size}")


        MasterDataPrefs.saveZones(this@LoginActivity, zones)
        MasterDataPrefs.saveWards(this@LoginActivity, wards)
        MasterDataPrefs.saveTypes(this@LoginActivity, types)
        MasterDataPrefs.saveCategories(this@LoginActivity, categories)
        MasterDataPrefs.saveUsage(this@LoginActivity, usage)
    }
}


