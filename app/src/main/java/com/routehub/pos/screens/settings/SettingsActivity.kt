package com.routehub.pos.screens.settings

import android.content.Intent
import com.routehub.pos.R
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.routehub.pos.helpers.LocaleHelper
import com.routehub.pos.screens.HomeActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val english = findViewById<Button>(R.id.btnEnglish)
        val hindi = findViewById<Button>(R.id.btnHindi)
        val punjabi = findViewById<Button>(R.id.btnPunjabi)

        english.setOnClickListener {

            LocaleHelper.setLocale(this, "en")
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()

        }

        hindi.setOnClickListener {

            LocaleHelper.setLocale(this, "hi")
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()

        }

        punjabi.setOnClickListener {

            LocaleHelper.setLocale(this, "pa")
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()

        }

    }
}