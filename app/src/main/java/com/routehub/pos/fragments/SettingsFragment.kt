package com.routehub.pos.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.routehub.pos.R
import com.routehub.pos.helpers.LocaleHelper
import com.routehub.pos.screens.HomeActivity
import com.routehub.pos.screens.settings.SettingsActivity

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

        super.onCreate(savedInstanceState)

//        setContentView(R.layout.activity_settings)

        val english = view.findViewById<Button>(R.id.btnEnglish)
        val hindi = view.findViewById<Button>(R.id.btnHindi)
        val punjabi = view.findViewById<Button>(R.id.btnPunjabi)

        english.setOnClickListener {

            LocaleHelper.setLocale(requireContext(), "en")
            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
//            finish()

        }

        hindi.setOnClickListener {

            LocaleHelper.setLocale(requireContext(), "hi")
            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
//            finish()

        }

        punjabi.setOnClickListener {

            LocaleHelper.setLocale(requireContext(), "pa")
            val intent = Intent(requireContext(), HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
//            finish()

        }

//        val btnSettings = view.findViewById<Button>(R.id.btnSettings)
//
//        btnSettings.setOnClickListener {
//
//            startActivity(
//                Intent(requireContext(), SettingsActivity::class.java)
//            )
//
//        }

        return view
    }
}