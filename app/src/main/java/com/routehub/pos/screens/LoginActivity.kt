package com.routehub.pos.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.routehub.pos.R
import com.routehub.pos.analytics.MixpanelManager
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.clients.SessionManager
import com.routehub.pos.models.LoginRequest
import com.routehub.pos.models.responses.LoginResponse
import com.routehub.pos.services.AuthService
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    val authService = ApiClient.retrofit.create(AuthService::class.java);

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
                        Log.d("LoginActivity", "Token: $token")
                        ("Token: $token")
                        SessionManager.setToken(token);
                        MixpanelManager.track("Login Success", request)
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
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
}


