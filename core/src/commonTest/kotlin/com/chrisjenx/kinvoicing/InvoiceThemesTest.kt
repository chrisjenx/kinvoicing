package com.chrisjenx.kinvoicing

import kotlin.test.*

class InvoiceThemesTest {

    @Test
    fun classicMatchesDefaults() {
        assertEquals(InvoiceStyle(), InvoiceThemes.Classic)
    }

    @Test
    fun allThemesListHasCorrectCount() {
        assertEquals(8, InvoiceThemes.all.size)
    }

    @Test
    fun allThemesHaveUniqueNames() {
        val names = InvoiceThemes.all.map { it.first }
        assertEquals(names.distinct().size, names.size, "Theme names must be unique")
    }

    @Test
    fun allThemesHaveUniquePrimaryColors() {
        val colors = InvoiceThemes.all.map { it.second.primaryColor }
        assertEquals(colors.distinct().size, colors.size, "Each theme should have a unique primary color")
    }

    @Test
    fun corporateHasAccentBorder() {
        assertTrue(InvoiceThemes.Corporate.accentBorder)
    }

    @Test
    fun boldUsesStackedLayoutAndGridLines() {
        assertEquals(HeaderLayout.STACKED, InvoiceThemes.Bold.headerLayout)
        assertTrue(InvoiceThemes.Bold.showGridLines)
        assertTrue(InvoiceThemes.Bold.accentBorder)
    }

    @Test
    fun warmUsesGeorgiaFont() {
        assertEquals("Georgia", InvoiceThemes.Warm.fontFamily)
    }

    @Test
    fun elegantUsesGeorgiaFontAndAccentBorder() {
        assertEquals("Georgia", InvoiceThemes.Elegant.fontFamily)
        assertTrue(InvoiceThemes.Elegant.accentBorder)
    }

    @Test
    fun minimalHasNoDecorations() {
        assertFalse(InvoiceThemes.Minimal.showGridLines)
        assertFalse(InvoiceThemes.Minimal.accentBorder)
        assertEquals(HeaderLayout.HORIZONTAL, InvoiceThemes.Minimal.headerLayout)
    }

    @Test
    fun allThemesHaveValidColors() {
        InvoiceThemes.all.forEach { (name, style) ->
            // All colors should have full alpha (0xFF prefix)
            assertTrue((style.primaryColor.value shr 24) and 0xFF == 0xFFL, "$name: primaryColor missing alpha")
            assertTrue((style.textColor.value shr 24) and 0xFF == 0xFFL, "$name: textColor missing alpha")
            assertTrue((style.negativeColor.value shr 24) and 0xFF == 0xFFL, "$name: negativeColor missing alpha")
        }
    }
}
