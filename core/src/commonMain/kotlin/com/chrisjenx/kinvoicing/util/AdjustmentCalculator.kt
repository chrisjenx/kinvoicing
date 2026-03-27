package com.chrisjenx.kinvoicing.util

import com.chrisjenx.kinvoicing.*

/**
 * Compute the display amount for this adjustment relative to a [subtotal].
 * Returns a signed value: negative for discounts/credits, positive for taxes/fees.
 */
public fun Adjustment.displayAmount(subtotal: Double): Double {
    return when (value) {
        is AdjustmentValue.Percent -> {
            val delta = value.applyTo(subtotal)
            when (type) {
                AdjustmentType.DISCOUNT, AdjustmentType.CREDIT -> -delta
                AdjustmentType.TAX, AdjustmentType.FEE, AdjustmentType.CUSTOM -> delta
            }
        }
        is AdjustmentValue.Fixed -> value.applyTo(subtotal)
        is AdjustmentValue.Absolute -> value.applyTo(subtotal)
    }
}

/**
 * Label with percentage suffix if this is a percent-based adjustment.
 * E.g., "Sales Tax (8.25%)" for a Percent, or just "Wire fee" for Fixed.
 */
public val Adjustment.labelWithPercent: String
    get() {
        val v = value
        return when (v) {
            is AdjustmentValue.Percent -> "$label (${v.rate}%)"
            else -> label
        }
    }
