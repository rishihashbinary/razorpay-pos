package com.routehub.pos.fee

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.routehub.pos.R
import com.routehub.pos.analytics.MixpanelManager
import java.math.BigDecimal
import java.math.RoundingMode

class FeeAdjustmentBottomSheet(
    private val originalMinor: Long,
    private val onApplied: (AppliedAdjustment) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var toggleMode: MaterialButtonToggleGroup
    private lateinit var edtValue: EditText
    private lateinit var txtPreviewAmount: TextView
    private lateinit var txtOriginalValue: TextView
    private lateinit var txtDiscountValue: TextView
    private lateinit var txtToCollectValue: TextView
    private lateinit var txtError: TextView
    private lateinit var btnApply: Button

    private var mode: AdjustmentType = AdjustmentType.PERCENTAGE
    private var lastValid: FeeResult.Valid? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bottomsheet_fee_adjustment, null)

        toggleMode = view.findViewById(R.id.toggleMode)
        edtValue = view.findViewById(R.id.edtValue)
        txtPreviewAmount = view.findViewById(R.id.txtPreviewAmount)
        txtOriginalValue = view.findViewById(R.id.txtOriginalValue)
        txtDiscountValue = view.findViewById(R.id.txtDiscountValue)
        txtToCollectValue = view.findViewById(R.id.txtToCollectValue)
        txtError = view.findViewById(R.id.txtError)
        btnApply = view.findViewById(R.id.btnApply)

        txtOriginalValue.text = FeeCalculator.formatMinor(originalMinor)

        toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            mode = when (checkedId) {
                R.id.btnModeAbsolute -> AdjustmentType.ABSOLUTE
                R.id.btnModeOverride -> AdjustmentType.OVERRIDE
                else -> AdjustmentType.PERCENTAGE
            }
            edtValue.hint = when (mode) {
                AdjustmentType.PERCENTAGE -> getString(R.string.fee_hint_percent)
                AdjustmentType.ABSOLUTE   -> getString(R.string.fee_hint_absolute)
                else                      -> getString(R.string.fee_hint_override)
            }
            edtValue.text = null
            recompute()
        }
        toggleMode.check(R.id.btnModePercent)

        edtValue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = recompute()
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        btnApply.setOnClickListener {
            val valid = lastValid ?: return@setOnClickListener
            val raw = edtValue.text.toString().trim()
            val applied = AppliedAdjustment(mode, raw, valid.finalMinor, valid.discountMinor)

            MixpanelManager.track(
                "Fee Adjusted",
                mapOf(
                    "adjustmentType" to mode.name,
                    "adjustmentValue" to raw,
                    "originalAmountMinor" to originalMinor,
                    "finalAmountMinor" to valid.finalMinor
                )
            )

            onApplied(applied)
            dismiss()
        }

        recompute()

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(view)
        return dialog
    }

    private fun recompute() {
        val raw = edtValue.text?.toString()?.trim().orEmpty()
        val adj = parse(raw, mode)
        val result = adj?.let { FeeCalculator.computeAdjustedFee(originalMinor, it) }

        when (result) {
            is FeeResult.Valid -> {
                lastValid = result
                txtPreviewAmount.text = FeeCalculator.formatMinor(result.finalMinor)
                txtDiscountValue.text = "− " + FeeCalculator.formatMinor(result.discountMinor)
                txtToCollectValue.text = FeeCalculator.formatMinor(result.finalMinor)
                txtError.text = ""
                btnApply.isEnabled = true
            }
            is FeeResult.Invalid -> {
                lastValid = null
                resetPreviewToOriginal()
                txtError.text = if (raw.isEmpty()) "" else result.reason
                btnApply.isEnabled = false
            }
            null -> {
                lastValid = null
                resetPreviewToOriginal()
                txtError.text = ""
                btnApply.isEnabled = false
            }
        }
    }

    private fun resetPreviewToOriginal() {
        txtPreviewAmount.text = FeeCalculator.formatMinor(originalMinor)
        txtDiscountValue.text = "− ₹0.00"
        txtToCollectValue.text = FeeCalculator.formatMinor(originalMinor)
    }

    private fun parse(raw: String, mode: AdjustmentType): FeeAdjustment? {
        if (raw.isEmpty()) return null
        val number = raw.toBigDecimalOrNull() ?: return null
        if (number <= BigDecimal.ZERO) return null
        return when (mode) {
            AdjustmentType.PERCENTAGE -> FeeAdjustment.Percentage(number)
            AdjustmentType.ABSOLUTE   -> FeeAdjustment.AbsoluteOff(rupeesToMinor(number))
            AdjustmentType.OVERRIDE   -> FeeAdjustment.Override(rupeesToMinor(number))
            AdjustmentType.NONE       -> null
        }
    }

    private fun rupeesToMinor(rupees: BigDecimal): Long =
        rupees.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
}