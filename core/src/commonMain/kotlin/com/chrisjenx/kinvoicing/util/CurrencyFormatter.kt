package com.chrisjenx.kinvoicing.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Currency formatting utility with support for 50+ ISO 4217 currencies.
 * Formats amounts with correct symbols, decimal places, and thousands separators.
 */
public object CurrencyFormatter {

    internal data class CurrencyInfo(
        val symbol: String,
        val decimalPlaces: Int = 2,
        val symbolPrefix: Boolean = true,
    )

    private val currencies: Map<String, CurrencyInfo> = mapOf(
        // Americas
        "USD" to CurrencyInfo("$"),
        "CAD" to CurrencyInfo("CA$"),
        "BRL" to CurrencyInfo("R$"),
        "MXN" to CurrencyInfo("MX$"),
        "ARS" to CurrencyInfo("ARS "),
        "CLP" to CurrencyInfo("CLP ", decimalPlaces = 0),
        "COP" to CurrencyInfo("COL$"),
        "PEN" to CurrencyInfo("S/"),
        "UYU" to CurrencyInfo("\$U"),

        // Europe
        "EUR" to CurrencyInfo("\u20AC"),
        "GBP" to CurrencyInfo("\u00A3"),
        "CHF" to CurrencyInfo("CHF "),
        "SEK" to CurrencyInfo(" kr", symbolPrefix = false),
        "NOK" to CurrencyInfo(" kr", symbolPrefix = false),
        "DKK" to CurrencyInfo(" kr", symbolPrefix = false),
        "PLN" to CurrencyInfo(" z\u0142", symbolPrefix = false),
        "CZK" to CurrencyInfo(" K\u010D", symbolPrefix = false),
        "HUF" to CurrencyInfo(" Ft", decimalPlaces = 0, symbolPrefix = false),
        "RON" to CurrencyInfo(" lei", symbolPrefix = false),
        "BGN" to CurrencyInfo(" лв", symbolPrefix = false),
        "HRK" to CurrencyInfo(" kn", symbolPrefix = false),
        "ISK" to CurrencyInfo(" kr", decimalPlaces = 0, symbolPrefix = false),
        "RUB" to CurrencyInfo(" \u20BD", symbolPrefix = false),
        "UAH" to CurrencyInfo(" \u20B4", symbolPrefix = false),
        "TRY" to CurrencyInfo("\u20BA"),

        // Asia-Pacific
        "JPY" to CurrencyInfo("\u00A5", decimalPlaces = 0),
        "CNY" to CurrencyInfo("\u00A5"),
        "HKD" to CurrencyInfo("HK$"),
        "TWD" to CurrencyInfo("NT$"),
        "KRW" to CurrencyInfo("\u20A9", decimalPlaces = 0),
        "SGD" to CurrencyInfo("S$"),
        "THB" to CurrencyInfo("\u0E3F"),
        "MYR" to CurrencyInfo("RM"),
        "IDR" to CurrencyInfo("Rp"),
        "PHP" to CurrencyInfo("\u20B1"),
        "VND" to CurrencyInfo("\u20AB", decimalPlaces = 0),
        "INR" to CurrencyInfo("\u20B9"),
        "PKR" to CurrencyInfo("Rs"),
        "BDT" to CurrencyInfo("\u09F3"),
        "LKR" to CurrencyInfo("Rs"),
        "AUD" to CurrencyInfo("A$"),
        "NZD" to CurrencyInfo("NZ$"),

        // Middle East / Africa
        "AED" to CurrencyInfo("AED "),
        "SAR" to CurrencyInfo("SAR "),
        "QAR" to CurrencyInfo("QAR "),
        "KWD" to CurrencyInfo("KD ", decimalPlaces = 3),
        "BHD" to CurrencyInfo("BD ", decimalPlaces = 3),
        "OMR" to CurrencyInfo("OMR ", decimalPlaces = 3),
        "EGP" to CurrencyInfo("E\u00A3"),
        "ZAR" to CurrencyInfo("R"),
        "NGN" to CurrencyInfo("\u20A6"),
        "KES" to CurrencyInfo("KSh"),
        "MAD" to CurrencyInfo("MAD "),
        "ILS" to CurrencyInfo("\u20AA"),
    )

    /**
     * Format an amount with the given currency code.
     * Uses correct decimal places per currency (e.g., 0 for JPY, 3 for BHD).
     * Negative amounts are displayed with a minus sign or parentheses.
     */
    public fun format(amount: Double, currency: String, useParentheses: Boolean = false): String {
        require(!amount.isNaN() && !amount.isInfinite()) { "Cannot format non-finite amount: $amount" }
        val info = currencies[currency] ?: CurrencyInfo(currency, decimalPlaces = 2)
        val isNegative = amount < 0
        val absAmount = abs(amount)

        val formatted = formatNumber(absAmount, info)

        return when {
            !isNegative -> formatted
            useParentheses -> "($formatted)"
            else -> "-$formatted"
        }
    }

    private fun formatNumber(amount: Double, info: CurrencyInfo): String {
        val scale = info.decimalPlaces
        val factor = pow10(scale)
        val units = (amount * factor).roundToLong()

        val whole = units / factor.toLong()
        val frac = units % factor.toLong()

        return buildString {
            if (info.symbolPrefix) append(info.symbol)
            append(whole.formatWithCommas())
            if (scale > 0) {
                append('.')
                append(frac.toString().padStart(scale, '0'))
            }
            if (!info.symbolPrefix) append(info.symbol)
        }
    }

    private fun pow10(n: Int): Double = 10.0.pow(n)

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
