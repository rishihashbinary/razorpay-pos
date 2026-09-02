package com.routehub.pos.fee

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class FeeCalculatorTest {

    @Test fun percentage_15pct_of_670() {
        val r = FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.Percentage(BigDecimal("15")))
        r as FeeResult.Valid
        assertEquals(56950, r.finalMinor)
        assertEquals(10050, r.discountMinor)
    }

    @Test fun percentage_halfUp_wholePaise() {
        val r = FeeCalculator.computeAdjustedFee(100, FeeAdjustment.Percentage(BigDecimal("33.333")))
        assertEquals(67, (r as FeeResult.Valid).finalMinor)
    }

    @Test fun percentage_boundsAreExclusive() {
        assertTrue(FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.Percentage(BigDecimal.ZERO)) is FeeResult.Invalid)
        assertTrue(FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.Percentage(BigDecimal("100"))) is FeeResult.Invalid)
    }

    @Test fun absolute_reducesCorrectly() {
        val r = FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.AbsoluteOff(10050))
        assertEquals(56950, (r as FeeResult.Valid).finalMinor)
    }

    @Test fun absolute_equalToOriginal_isInvalid_zeroNotAllowed() {
        assertTrue(FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.AbsoluteOff(67000)) is FeeResult.Invalid)
    }

    @Test fun override_mustBeStrictlyLowerAndPositive() {
        assertTrue(FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.Override(67000)) is FeeResult.Invalid)
        assertTrue(FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.Override(70000)) is FeeResult.Invalid)
        assertTrue(FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.Override(0)) is FeeResult.Invalid)
        assertEquals(56950, (FeeCalculator.computeAdjustedFee(67000, FeeAdjustment.Override(56950)) as FeeResult.Valid).finalMinor)
    }
}