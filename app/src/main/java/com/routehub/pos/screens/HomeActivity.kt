package com.routehub.pos.screens


import com.routehub.pos.R
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.routehub.pos.helpers.LocaleHelper
import com.routehub.pos.screens.settings.SettingsActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        LocaleHelper.loadLocale(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val settings = findViewById<Button>(R.id.btnSettings)

        settings.setOnClickListener {

            startActivity(Intent(this, SettingsActivity::class.java))

        }
    }
}