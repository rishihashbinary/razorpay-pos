package com.routehub.pos.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import com.routehub.pos.R
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