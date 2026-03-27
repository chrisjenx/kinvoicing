package com.chrisjenx.invoicekit.util

/**
 * Format a Double as a quantity string: whole numbers drop the decimal,
 * fractional values keep it. E.g., 10.0 → "10", 10.5 → "10.5".
 */
public fun Double.formatAsQuantity(): String {
    return if (this == toLong().toDouble()) toLong().toString()
    else toString()
}
