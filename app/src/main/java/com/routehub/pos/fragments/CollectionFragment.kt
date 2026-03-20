package com.routehub.pos.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.eze.api.EzeAPI
import com.routehub.pos.R
import com.routehub.pos.screens.PropertyDetailsActivity
import com.routehub.pos.screens.PropertySearchActivity
import com.routehub.pos.screens.ScanQrActivity
import org.json.JSONObject

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
        val testPrint = view.findViewById<TextView>(R.id.tvTestPrint)

//        mobileSearch.isEnabled = false
        addProperty.isVisible = false


        qrScan.setOnClickListener {

            val intent = Intent(requireContext(), ScanQrActivity::class.java)
//
//            val intent = Intent(requireContext(), PropertyDetailsActivity::class.java)
//            intent.putExtra("qrCode", "ASR-174-885")
            startActivity(intent)


        }

        mobileSearch.setOnClickListener {

            val intent = Intent(requireContext(), PropertySearchActivity::class.java)
            startActivity(intent)


        }

        testPrint.setOnClickListener {
            val receipt = JSONObject()
            receipt.put("amount", "500")
            receipt.put("txnId", "123456")
            EzeAPI.printBitmap(requireContext(), 10028, receipt);
        }

        return view
    }
}