package com.chrisjenx.kinvoicing.util

import kotlin.test.*

class NumberFormatterTest {

    @Test
    fun wholeNumberFormatsWithoutDecimal() {
        assertEquals("10", 10.0.formatAsQuantity())
    }

    @Test
    fun zeroFormatsAsZero() {
        assertEquals("0", 0.0.formatAsQuantity())
    }

    @Test
    fun largeWholeNumber() {
        assertEquals("1000", 1000.0.formatAsQuantity())
    }

    @Test
    fun decimalPreserved() {
        assertEquals("10.5", 10.5.formatAsQuantity())
    }

    @Test
    fun smallDecimal() {
        assertEquals("0.25", 0.25.formatAsQuantity())
    }

    @Test
    fun negativeWholeNumber() {
        assertEquals("-5", (-5.0).formatAsQuantity())
    }

    @Test
    fun negativeDecimal() {
        assertEquals("-3.5", (-3.5).formatAsQuantity())
    }
}
