package com.routehub.pos.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.routehub.pos.R
import com.routehub.pos.fragments.CollectionFragment
import com.routehub.pos.fragments.SettingsFragment
import com.routehub.pos.helpers.LocaleHelper

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        LocaleHelper.loadLocale(this)

        super.onCreate(savedInstanceState)

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
}