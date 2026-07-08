package com.routehub.pos.fragments.settings

import android.content.Intent
import com.routehub.pos.screens.LoginActivity
import com.routehub.pos.utils.Session
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.routehub.pos.R
import com.routehub.pos.helpers.LocaleHelper
import com.routehub.pos.models.settings.SettingItem
import com.routehub.pos.models.settings.SettingType
import com.routehub.pos.screens.HomeActivity

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_settings,
            container,
            false
        )

        val recycler = view.findViewById<RecyclerView>(R.id.settingsRecycler)

        val settingsList = listOf(
            SettingItem(
                id = "language",
                title = getString(R.string.change_language),
                summary = getCurrentLanguage(),
                type = SettingType.LANGUAGE
            ),
            SettingItem(
                id = "logout",
                title = getString(R.string.logout),
                summary = "",
                type = SettingType.LOGOUT
            )
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())

        recycler.adapter = SettingsAdapter(settingsList) { item ->
            if (item.id == "language") {
                showLanguageDialog()
            } else if (item.id == "logout") {
                showLogoutConfirmation()
            }
        }

        return view
    }

    private fun showLanguageDialog() {

        val languages = arrayOf(
            "English",
            "Hindi",
            "Punjabi"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setItems(languages) { _, which ->

                when(which) {

                    0 -> changeLanguage("en")
                    1 -> changeLanguage("hi")
                    2 -> changeLanguage("pa")
                }

            }
            .show()
    }

    private fun changeLanguage(lang: String) {

        LocaleHelper.setLocale(requireContext(), lang)

        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_confirmation_message))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                Session.clear()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun getCurrentLanguage(): String {

        val lang = LocaleHelper.getLanguage(requireContext())

        Log.d("SettingsFragment", "Current Language: $lang")

        return when(lang) {

            "hi" -> "Hindi"
            "pa" -> "Punjabi"
            else -> "English"
        }
    }
}