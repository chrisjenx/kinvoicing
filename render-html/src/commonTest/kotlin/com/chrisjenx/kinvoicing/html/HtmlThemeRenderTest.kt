package com.chrisjenx.kinvoicing.html

import com.chrisjenx.kinvoicing.*
import kotlinx.datetime.LocalDate
import kotlin.test.*

class HtmlThemeRenderTest {

    private val renderer = HtmlRenderer()

    // ── Theme color propagation ──

    @Test
    fun themedInvoiceUsesThemeColors() {
        val doc = invoice {
            style { theme(InvoiceThemes.Fresh) }
            header {
                branding { primary { name("Test Co") } }
                invoiceNumber("INV-001")
            }
            billTo { name("Customer") }
            lineItems { item("Service", amount = 100.0) }
            summary {}
        }
        val html = renderer.render(doc)
        // Fresh primary color #059669 should appear in styles
        assertTrue("059669" in html.uppercase(), "Fresh theme primary color should appear")
    }

    @Test
    fun semanticColorsFromStyleNotHardcoded() {
        val doc = invoice {
            style {
                theme(InvoiceThemes.Warm)
            }
            billTo { name("Customer") }
            lineItems { item("Service", amount = 100.0) }
            summary {}
            footer { notes("Thank you") }
        }
        val html = renderer.render(doc)
        // Warm mutedBackgroundColor is #FEF3C7, should appear in footer/meta backgrounds
        assertTrue("FEF3C7" in html.uppercase(), "Warm theme muted background should appear in footer")
    }

    @Test
    fun allThemesRenderWithoutError() {
        InvoiceThemes.all.forEach { (name, theme) ->
            val doc = invoice {
                style { theme(theme) }
                header {
                    branding { primary { name("Test") } }
                    invoiceNumber("INV-$name")
                }
                billTo { name("Customer") }
                lineItems { item("Service", amount = 100.0) }
                summary {}
            }
            val html = renderer.render(doc)
            assertTrue(html.isNotBlank(), "Theme '$name' should render non-empty HTML")
            assertTrue("INV-$name" in html, "Theme '$name' should contain invoice number")
        }
    }

    // ── Side-by-side BillFrom + BillTo ──

    @Test
    fun adjacentPartiesRenderSideBySide() {
        val doc = invoice {
            header { invoiceNumber("INV-001") }
            billFrom {
                name("Seller Corp")
                address("123 Sell St")
            }
            billTo {
                name("Buyer Inc")
                address("456 Buy Ave")
            }
            lineItems { item("Service", amount = 100.0) }
            summary {}
        }
        val html = renderer.render(doc)
        // Should contain both names
        assertTrue("Seller Corp" in html)
        assertTrue("Buyer Inc" in html)
        // Side-by-side uses a table with two 50% width cells
        assertTrue("50%" in html, "Side-by-side parties should use 50% width cells")
    }

    // ── STACKED header layout ──

    @Test
    fun stackedHeaderRendersWithoutError() {
        val doc = invoice {
            style {
                headerLayout = HeaderLayout.STACKED
                logoPlacement = LogoPlacement.CENTER
            }
            header {
                branding { primary { name("Stacked Co") } }
                invoiceNumber("INV-STACK")
                issueDate(LocalDate(2026, 3, 1))
            }
            billTo { name("Customer") }
            lineItems { item("Service", amount = 100.0) }
            summary {}
        }
        val html = renderer.render(doc)
        assertTrue("Stacked Co" in html)
        assertTrue("INV-STACK" in html)
        // Stacked layout uses text-align: center for CENTER placement
        assertTrue("text-align: center" in html, "CENTER logo placement should center-align")
    }

    // ── Summary total emphasis ──

    @Test
    fun summaryTotalHasLargerFontAndPrimaryColor() {
        val doc = invoice {
            billTo { name("Customer") }
            lineItems { item("Service", amount = 100.0) }
            summary { currency("USD") }
        }
        val html = renderer.render(doc)
        // Total should use 20px font size (upgraded from 16px)
        assertTrue("font-size: 20px" in html, "Total should use 20px font")
    }

    @Test
    fun summaryTotalUsesThemePrimaryColor() {
        val doc = invoice {
            style { theme(InvoiceThemes.Corporate) }
            billTo { name("Customer") }
            lineItems { item("Service", amount = 100.0) }
            summary { currency("USD") }
        }
        val html = renderer.render(doc)
        // Corporate primary is #1E3A5F — should appear in the total row
        assertTrue("1E3A5F" in html.uppercase(), "Total should use theme primary color")
    }

    // ── Footer labels ──

    @Test
    fun footerRendersNotesAndTermsLabels() {
        val doc = invoice {
            billTo { name("Customer") }
            lineItems { item("Service", amount = 100.0) }
            summary {}
            footer {
                notes("Thank you!")
                terms("Net 30")
            }
        }
        val html = renderer.render(doc)
        assertTrue("Notes" in html || "NOTES" in html, "Footer should have a Notes label")
        assertTrue("Terms" in html || "TERMS" in html, "Footer should have a Terms label")
        assertTrue("Thank you!" in html)
        assertTrue("Net 30" in html)
    }

    @Test
    fun footerUsesMutedBackground() {
        val doc = invoice {
            style { theme(InvoiceThemes.Fresh) }
            billTo { name("Customer") }
            lineItems { item("Service", amount = 100.0) }
            summary {}
            footer { notes("Thank you") }
        }
        val html = renderer.render(doc)
        // Fresh mutedBackgroundColor is #ECFDF5
        assertTrue("ECFDF5" in html.uppercase(), "Footer should use theme muted background")
    }
}
