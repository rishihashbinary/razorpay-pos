package com.routehub.pos.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
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

        val btnCollectFee = view.findViewById<Button>(R.id.btnCollectFee)

        btnCollectFee.setOnClickListener {

//            Intent(requireContext(), ScanQrActivity::class.java)

            val intent = Intent(requireContext(), PropertyDetailsActivity::class.java)
            intent.putExtra("qrCode", "ASR-182-942")
            startActivity(intent)


        }

        return view
    }
}