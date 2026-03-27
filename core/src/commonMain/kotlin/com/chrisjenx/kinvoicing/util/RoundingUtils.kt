package com.chrisjenx.kinvoicing.util

import kotlin.math.pow
import kotlin.math.round

/**
 * Rounds this Double to the given number of decimal places.
 * Used for intermediate monetary calculations to prevent IEEE 754 drift.
 */
internal fun Double.roundToScale(scale: Int = 2): Double {
    val factor = 10.0.pow(scale)
    return round(this * factor) / factor
}
