package com.chrisjenx.kinvoicing.util

import com.chrisjenx.kinvoicing.*
import kotlin.math.abs
import kotlin.test.*

class AdjustmentCalculatorTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.01) {
        assertTrue(abs(expected - actual) < tolerance, "Expected $expected but got $actual")
    }

    // --- AdjustmentValue.applyTo ---

    @Test
    fun percentApplyTo() {
        val value = AdjustmentValue.Percent(10.0)
        assertClose(100.0, value.applyTo(1000.0))
    }

    @Test
    fun percentApplyToZeroBase() {
        val value = AdjustmentValue.Percent(50.0)
        assertClose(0.0, value.applyTo(0.0))
    }

    @Test
    fun fixedApplyTo() {
        val value = AdjustmentValue.Fixed(-50.0)
        assertClose(-50.0, value.applyTo(1000.0))
    }

    @Test
    fun fixedApplyToIgnoresBase() {
        val value = AdjustmentValue.Fixed(25.0)
        assertClose(25.0, value.applyTo(0.0))
        assertClose(25.0, value.applyTo(999.0))
    }

    @Test
    fun absoluteApplyTo() {
        val value = AdjustmentValue.Absolute(500.0)
        assertClose(500.0, value.applyTo(1000.0))
    }

    // --- Adjustment.displayAmount ---

    @Test
    fun percentDiscountDisplayAmount() {
        val adj = Adjustment("10% off", AdjustmentType.DISCOUNT, AdjustmentValue.Percent(10.0))
        assertClose(-100.0, adj.displayAmount(1000.0))
    }

    @Test
    fun percentTaxDisplayAmount() {
        val adj = Adjustment("Tax", AdjustmentType.TAX, AdjustmentValue.Percent(8.0))
        assertClose(80.0, adj.displayAmount(1000.0))
    }

    @Test
    fun percentFeeDisplayAmount() {
        val adj = Adjustment("Fee", AdjustmentType.FEE, AdjustmentValue.Percent(5.0))
        assertClose(50.0, adj.displayAmount(1000.0))
    }

    @Test
    fun percentCreditDisplayAmount() {
        val adj = Adjustment("Credit", AdjustmentType.CREDIT, AdjustmentValue.Percent(10.0))
        assertClose(-100.0, adj.displayAmount(1000.0))
    }

    @Test
    fun fixedDiscountDisplayAmount() {
        val adj = Adjustment("Flat off", AdjustmentType.DISCOUNT, AdjustmentValue.Fixed(-50.0))
        assertClose(-50.0, adj.displayAmount(1000.0))
    }

    @Test
    fun fixedFeeDisplayAmount() {
        val adj = Adjustment("Wire fee", AdjustmentType.FEE, AdjustmentValue.Fixed(25.0))
        assertClose(25.0, adj.displayAmount(1000.0))
    }

    @Test
    fun absoluteDisplayAmount() {
        val adj = Adjustment("Override", AdjustmentType.CUSTOM, AdjustmentValue.Absolute(500.0))
        assertClose(500.0, adj.displayAmount(1000.0))
    }

    // --- Adjustment.percentLabel ---

    @Test
    fun percentLabelForPercentValue() {
        val adj = Adjustment("Tax", AdjustmentType.TAX, AdjustmentValue.Percent(8.25))
        assertEquals("Tax (8.25%)", adj.labelWithPercent)
    }

    @Test
    fun percentLabelForFixedValue() {
        val adj = Adjustment("Fee", AdjustmentType.FEE, AdjustmentValue.Fixed(25.0))
        assertEquals("Fee", adj.labelWithPercent)
    }
}
