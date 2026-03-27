package com.chrisjenx.invoicekit

import com.chrisjenx.invoicekit.util.CurrencyFormatter
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
}
