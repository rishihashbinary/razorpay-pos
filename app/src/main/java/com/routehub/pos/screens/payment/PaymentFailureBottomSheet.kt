package com.routehub.pos.screens.payment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.routehub.pos.R
import com.routehub.pos.clients.ApiClient
import com.routehub.pos.models.Reason
import com.routehub.pos.models.responses.ApiResponse
import com.routehub.pos.services.PaymentService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentFailureBottomSheet(
    private val onSubmit: (reason: String, remarks: String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var edtRemarks: EditText
    private lateinit var btnSubmit: Button

    val paymentService = ApiClient.retrofit.create(PaymentService::class.java)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bottomsheet_payment_failure, null)

        radioGroup = view.findViewById(R.id.radioGroupReasons)
        edtRemarks = view.findViewById(R.id.edtRemarks)
        btnSubmit = view.findViewById(R.id.btnSubmitFailure)

        setupRadioButtons()

        btnSubmit.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(context, "Please select a reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadio = view.findViewById<RadioButton>(selectedId)
            val reason = selectedRadio.tag.toString()
            val remarks = edtRemarks.text.toString()

            onSubmit(reason, remarks)
            dismiss()
        }

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(view)
        return dialog
    }

    private fun setupRadioButtons() {

        paymentService.getDenialReasons().enqueue(object : Callback<ApiResponse<Reason>> {
            override fun onResponse(call: Call<ApiResponse<Reason>>, response: Response<ApiResponse<Reason>>) {
                if (response.isSuccessful) {
                    val reasons = response.body()?.data as List<Reason>
                    reasons.forEach { reason ->
                        val radioButton = RadioButton(context)
                        radioButton.text = reason.reason
                        radioButton.tag = reason.reason
                        radioGroup.addView(radioButton)
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<Reason>>, t: Throwable) {
                t.printStackTrace()
            }
        });
//        PaymentFailureReason.values().forEach { reason ->
//            val radioButton = RadioButton(context)
//            radioButton.text = reason.displayName
//            radioButton.tag = reason.name
//            radioGroup.addView(radioButton)
//        }
    }
}