package com.chrisjenx.kinvoicing

import com.chrisjenx.kinvoicing.util.CurrencyFormatter
import kotlin.test.*

class CurrencyFormatterTest {

    @Test
    fun formatUsd() {
        assertEquals("$100.00", CurrencyFormatter.format(100.0, "USD"))
    }

    @Test
    fun formatWithDecimals() {
        assertEquals("$99.99", CurrencyFormatter.format(99.99, "USD"))
    }

    @Test
    fun formatLargeAmount() {
        assertEquals("$1,234,567.89", CurrencyFormatter.format(1234567.89, "USD"))
    }

    @Test
    fun formatNegativeWithMinus() {
        assertEquals("-$50.00", CurrencyFormatter.format(-50.0, "USD"))
    }

    @Test
    fun formatNegativeWithParentheses() {
        assertEquals("($50.00)", CurrencyFormatter.format(-50.0, "USD", useParentheses = true))
    }

    @Test
    fun formatZero() {
        assertEquals("$0.00", CurrencyFormatter.format(0.0, "USD"))
    }

    @Test
    fun formatEur() {
        assertEquals("\u20AC100.00", CurrencyFormatter.format(100.0, "EUR"))
    }

    @Test
    fun formatGbp() {
        assertEquals("\u00A3100.00", CurrencyFormatter.format(100.0, "GBP"))
    }

    @Test
    fun formatUnknownCurrencyUsesCode() {
        assertEquals("XYZ100.00", CurrencyFormatter.format(100.0, "XYZ"))
    }

    @Test
    fun formatSmallAmount() {
        assertEquals("$0.01", CurrencyFormatter.format(0.01, "USD"))
    }

    @Test
    fun formatWithCommas() {
        assertEquals("$10,000.00", CurrencyFormatter.format(10000.0, "USD"))
    }

    // ── Zero-decimal currencies ──

    @Test
    fun formatJpyZeroDecimals() {
        assertEquals("\u00A51,000", CurrencyFormatter.format(1000.0, "JPY"))
    }

    @Test
    fun formatKrwZeroDecimals() {
        assertEquals("\u20A950,000", CurrencyFormatter.format(50000.0, "KRW"))
    }

    @Test
    fun formatHufZeroDecimalsSuffix() {
        assertEquals("1,000 Ft", CurrencyFormatter.format(1000.0, "HUF"))
    }

    @Test
    fun formatIskZeroDecimalsSuffix() {
        assertEquals("5,000 kr", CurrencyFormatter.format(5000.0, "ISK"))
    }

    @Test
    fun formatClpZeroDecimals() {
        assertEquals("CLP 10,000", CurrencyFormatter.format(10000.0, "CLP"))
    }

    @Test
    fun formatVndZeroDecimals() {
        assertEquals("\u20AB100,000", CurrencyFormatter.format(100000.0, "VND"))
    }

    // ── Three-decimal currencies ──

    @Test
    fun formatBhdThreeDecimals() {
        assertEquals("BD 1.500", CurrencyFormatter.format(1.5, "BHD"))
    }

    @Test
    fun formatKwdThreeDecimals() {
        assertEquals("KD 25.750", CurrencyFormatter.format(25.75, "KWD"))
    }

    @Test
    fun formatOmrThreeDecimals() {
        assertEquals("OMR 100.000", CurrencyFormatter.format(100.0, "OMR"))
    }

    // ── Suffix-symbol currencies ──

    @Test
    fun formatSekSuffix() {
        assertEquals("1,000.00 kr", CurrencyFormatter.format(1000.0, "SEK"))
    }

    @Test
    fun formatPlnSuffix() {
        assertEquals("500.00 z\u0142", CurrencyFormatter.format(500.0, "PLN"))
    }

    @Test
    fun formatCzkSuffix() {
        assertEquals("250.00 K\u010D", CurrencyFormatter.format(250.0, "CZK"))
    }

    // ── Additional currencies ──

    @Test
    fun formatInr() {
        assertEquals("\u20B91,000.00", CurrencyFormatter.format(1000.0, "INR"))
    }

    @Test
    fun formatCny() {
        assertEquals("\u00A5500.00", CurrencyFormatter.format(500.0, "CNY"))
    }

    @Test
    fun formatZar() {
        assertEquals("R1,000.00", CurrencyFormatter.format(1000.0, "ZAR"))
    }

    @Test
    fun formatSgd() {
        assertEquals("S$100.00", CurrencyFormatter.format(100.0, "SGD"))
    }

    @Test
    fun formatChf() {
        assertEquals("CHF 100.00", CurrencyFormatter.format(100.0, "CHF"))
    }

    @Test
    fun formatTry() {
        assertEquals("\u20BA100.00", CurrencyFormatter.format(100.0, "TRY"))
    }
}
