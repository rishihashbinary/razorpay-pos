package com.routehub.pos.fee

import java.math.BigDecimal
import java.math.RoundingMode

enum class AdjustmentType { PERCENTAGE, ABSOLUTE, OVERRIDE, NONE }

sealed interface FeeAdjustment {
    data class Percentage(val percent: BigDecimal) : FeeAdjustment
    data class AbsoluteOff(val offMinor: Long) : FeeAdjustment
    data class Override(val newMinor: Long) : FeeAdjustment
}

sealed interface FeeResult {
    data class Valid(val finalMinor: Long, val discountMinor: Long) : FeeResult
    data class Invalid(val reason: String) : FeeResult
}

object FeeCalculator {

    fun computeAdjustedFee(originalMinor: Long, adj: FeeAdjustment): FeeResult {
        require(originalMinor > 0) { "originalMinor must be > 0" }
        val finalMinor = when (adj) {
            is FeeAdjustment.Percentage -> {
                if (adj.percent <= BigDecimal.ZERO || adj.percent >= BigDecimal(100))
                    return FeeResult.Invalid("Percentage must be between 0 and 100")
                val discount = BigDecimal(originalMinor)
                    .multiply(adj.percent)
                    .divide(BigDecimal(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .toLong()
                originalMinor - discount
            }
            is FeeAdjustment.AbsoluteOff -> originalMinor - adj.offMinor
            is FeeAdjustment.Override    -> adj.newMinor
        }
        return when {
            finalMinor <= 0             -> FeeResult.Invalid("Amount must be greater than zero")
            finalMinor >= originalMinor -> FeeResult.Invalid("Amount cannot be increased")
            else -> FeeResult.Valid(finalMinor, originalMinor - finalMinor)
        }
    }


    fun rupeesToMinor(rupees: Float): Long =
        BigDecimal(rupees.toString())
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()
    fun rupeesToMinor(rupees: Double): Long =
        BigDecimal(rupees.toString())
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()

    fun minorToRupeesFloat(minor: Long): Float =
        BigDecimal(minor).movePointLeft(2).toFloat()

    fun formatMinor(minor: Long): String =
        "₹" + BigDecimal(minor).movePointLeft(2).setScale(2, RoundingMode.HALF_UP).toPlainString()
}

data class AppliedAdjustment(
    val type: AdjustmentType,
    val rawValue: String,
    val finalMinor: Long,
    val discountMinor: Long
)