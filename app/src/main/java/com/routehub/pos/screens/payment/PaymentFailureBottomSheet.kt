package com.routehub.pos.screens.payment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.routehub.pos.R

class PaymentFailureBottomSheet(
    private val onSubmit: (reason: PaymentFailureReason, remarks: String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var edtRemarks: EditText
    private lateinit var btnSubmit: Button

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
            val reason = PaymentFailureReason.valueOf(selectedRadio.tag.toString())
            val remarks = edtRemarks.text.toString()

            onSubmit(reason, remarks)
            dismiss()
        }

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(view)
        return dialog
    }

    private fun setupRadioButtons() {
        PaymentFailureReason.values().forEach { reason ->
            val radioButton = RadioButton(context)
            radioButton.text = reason.displayName
            radioButton.tag = reason.name
            radioGroup.addView(radioButton)
        }
    }
}