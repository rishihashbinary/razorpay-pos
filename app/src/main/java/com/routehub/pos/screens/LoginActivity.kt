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
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "initializing MixPanel")
        MixpanelManager.initialize(this,  "33f0937bb8ec54f0a6b39d404275ffd1")

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)

        loginBtn.setOnClickListener {

            val user = "admin" //username.text.toString()
            val pass = "123" //password.text.toString()

            if (user == "admin" && pass == "123") {
                val props = JSONObject()
                props.put("user", user)
                MixpanelManager.track("Login Success", props)
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}