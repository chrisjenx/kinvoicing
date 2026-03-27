package com.chrisjenx.invoicekit.util

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Simple currency formatting utility. Formats amounts with currency symbol and two decimal places.
 */
public object CurrencyFormatter {

    private val currencySymbols: Map<String, String> = mapOf(
        "USD" to "$",
        "EUR" to "\u20AC",
        "GBP" to "\u00A3",
        "JPY" to "\u00A5",
        "CAD" to "CA$",
        "AUD" to "A$",
        "CHF" to "CHF",
        "CNY" to "\u00A5",
        "INR" to "\u20B9",
        "BRL" to "R$",
    )

    /**
     * Format an amount with the given currency code.
     * Negative amounts are displayed with a minus sign or parentheses.
     */
    public fun format(amount: Double, currency: String, useParentheses: Boolean = false): String {
        val symbol = currencySymbols[currency] ?: currency
        val isNegative = amount < 0
        val absAmount = abs(amount)

        // Round to 2 decimal places
        val cents = (absAmount * 100).roundToLong()
        val whole = cents / 100
        val frac = cents % 100

        val formatted = buildString {
            append(symbol)
            append(whole.formatWithCommas())
            append('.')
            append(frac.toString().padStart(2, '0'))
        }

        return when {
            !isNegative -> formatted
            useParentheses -> "($formatted)"
            else -> "-$formatted"
        }
    }

    private fun Long.formatWithCommas(): String {
        val s = this.toString()
        if (s.length <= 3) return s
        return buildString {
            s.reversed().forEachIndexed { i, c ->
                if (i > 0 && i % 3 == 0) append(',')
                append(c)
            }
        }.reversed()
    }
}
