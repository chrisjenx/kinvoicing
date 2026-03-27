package com.chrisjenx.kinvoicing

import kotlin.test.*

class ArgbColorTest {

    @Test
    fun constructFromHexLiteral() {
        val color = ArgbColor(0xFF2563EB)
        assertEquals(0xFF2563EB, color.value)
    }

    @Test
    fun toHexColorPrimaryBlue() {
        assertEquals("#2563EB", ArgbColor(0xFF2563EB).toHexColor())
    }

    @Test
    fun toHexColorWhite() {
        assertEquals("#FFFFFF", ArgbColor(0xFFFFFFFF).toHexColor())
    }

    @Test
    fun toHexColorBlack() {
        assertEquals("#000000", ArgbColor(0xFF000000).toHexColor())
    }

    @Test
    fun toHexColorNegativeRed() {
        assertEquals("#DC2626", ArgbColor(0xFFDC2626).toHexColor())
    }

    @Test
    fun valueClassEquality() {
        assertEquals(ArgbColor(0xFF2563EB), ArgbColor(0xFF2563EB))
        assertNotEquals(ArgbColor(0xFF2563EB), ArgbColor(0xFFFFFFFF))
    }

    @Test
    fun defaultStyleColorsConvert() {
        val style = InvoiceStyle()
        assertEquals("#2563EB", style.primaryColor.toHexColor())
        assertEquals("#64748B", style.secondaryColor.toHexColor())
        assertEquals("#1E293B", style.textColor.toHexColor())
        assertEquals("#FFFFFF", style.backgroundColor.toHexColor())
    }
}
