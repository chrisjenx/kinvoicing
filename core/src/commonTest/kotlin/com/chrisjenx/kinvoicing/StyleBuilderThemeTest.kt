package com.chrisjenx.kinvoicing

import kotlin.test.*

class StyleBuilderThemeTest {

    @Test
    fun themeAppliesAllProperties() {
        val doc = invoice {
            style { theme(InvoiceThemes.Elegant) }
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        assertEquals(InvoiceThemes.Elegant, doc.style)
    }

    @Test
    fun themeCanBeOverridden() {
        val doc = invoice {
            style {
                theme(InvoiceThemes.Corporate)
                showGridLines = true
            }
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        // Grid lines overridden, rest matches Corporate
        assertTrue(doc.style.showGridLines)
        assertEquals(InvoiceThemes.Corporate.primaryColor, doc.style.primaryColor)
        assertEquals(InvoiceThemes.Corporate.negativeColor, doc.style.negativeColor)
        assertEquals(InvoiceThemes.Corporate.borderColor, doc.style.borderColor)
        assertTrue(doc.style.accentBorder) // from Corporate
    }

    @Test
    fun newSemanticColorPropertiesHaveCorrectDefaults() {
        val style = InvoiceStyle()
        assertEquals(ArgbColor(0xFFDC2626), style.negativeColor)
        assertEquals(ArgbColor(0xFFE2E8F0), style.borderColor)
        assertEquals(ArgbColor(0xFFF1F5F9), style.dividerColor)
        assertEquals(ArgbColor(0xFFF8FAFC), style.mutedBackgroundColor)
        assertEquals(ArgbColor(0xFFFFFFFF), style.surfaceColor)
    }

    @Test
    fun semanticColorsSetViaDsl() {
        val doc = invoice {
            style {
                negativeColor = 0xFFFF0000
                borderColor = 0xFF111111
                dividerColor = 0xFF222222
                mutedBackgroundColor = 0xFF333333
                surfaceColor = 0xFF444444
            }
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        assertEquals(ArgbColor(0xFFFF0000), doc.style.negativeColor)
        assertEquals(ArgbColor(0xFF111111), doc.style.borderColor)
        assertEquals(ArgbColor(0xFF222222), doc.style.dividerColor)
        assertEquals(ArgbColor(0xFF333333), doc.style.mutedBackgroundColor)
        assertEquals(ArgbColor(0xFF444444), doc.style.surfaceColor)
    }

    @Test
    fun themeOverridesSingleColor() {
        val doc = invoice {
            style {
                theme(InvoiceThemes.Fresh)
                negativeColor = 0xFFAA0000
            }
            billTo { name("Test") }
            lineItems { item("Item", amount = 1.0) }
            summary {}
        }
        // negativeColor overridden
        assertEquals(ArgbColor(0xFFAA0000), doc.style.negativeColor)
        // rest from Fresh theme
        assertEquals(InvoiceThemes.Fresh.primaryColor, doc.style.primaryColor)
        assertEquals(InvoiceThemes.Fresh.borderColor, doc.style.borderColor)
    }
}
