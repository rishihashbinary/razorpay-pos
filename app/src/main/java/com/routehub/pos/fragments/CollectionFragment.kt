package com.routehub.pos.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.routehub.pos.R
import com.routehub.pos.screens.PropertyDetailsActivity
import com.routehub.pos.screens.ScanQrActivity

class CollectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_collection,
            container,
            false
        )

        val qrScan = view.findViewById<View>(R.id.btnQrScan)
        val mobileSearch = view.findViewById<View>(R.id.btnMobileSearch)
        val addProperty = view.findViewById<TextView>(R.id.tvAddProperty)


        qrScan.setOnClickListener {

//            val intent = Intent(requireContext(), ScanQrActivity::class.java)
//
            val intent = Intent(requireContext(), PropertyDetailsActivity::class.java)
            intent.putExtra("qrCode", "ASR-001-198")
            startActivity(intent)


        }

        return view
    }
}