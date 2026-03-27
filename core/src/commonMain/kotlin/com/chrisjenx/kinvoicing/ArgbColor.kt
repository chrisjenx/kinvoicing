package com.chrisjenx.kinvoicing

import kotlin.jvm.JvmInline

/**
 * An ARGB color stored as a [Long] (e.g., `0xFF2563EB`).
 * Provides conversion to CSS hex and Compose Color representations.
 */
@JvmInline
public value class ArgbColor(public val value: Long) {
    /** Convert to a CSS hex color string (e.g., "#2563EB"). Alpha channel is ignored. */
    public fun toHexColor(): String {
        val r = (value shr 16 and 0xFF).toInt()
        val g = (value shr 8 and 0xFF).toInt()
        val b = (value and 0xFF).toInt()
        return "#${r.hex()}${g.hex()}${b.hex()}"
    }
}

private fun Int.hex(): String = this.toString(16).padStart(2, '0').uppercase()
