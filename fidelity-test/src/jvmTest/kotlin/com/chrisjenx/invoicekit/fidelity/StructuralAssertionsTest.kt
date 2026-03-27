package com.chrisjenx.invoicekit.fidelity

import com.chrisjenx.invoicekit.InvoiceFixtures
import com.chrisjenx.invoicekit.InvoiceSection
import com.chrisjenx.invoicekit.html.toHtml
import com.chrisjenx.invoicekit.pdf.toPdf
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import kotlin.test.*

/**
 * Non-visual structural assertions for PDF and HTML output.
 */
class StructuralAssertionsTest {

    // --- PDF structural assertions ---

    @Test
    fun pdfHasCorrectPageCount() {
        val basic = InvoiceFixtures.basic.toPdf()
        Loader.loadPDF(basic).use {
            assertEquals(1, it.numberOfPages, "Basic invoice should be 1 page")
        }

        val long = InvoiceFixtures.long.toPdf()
        Loader.loadPDF(long).use {
            assertTrue(it.numberOfPages > 1, "Long invoice should be multi-page")
        }
    }

    @Test
    fun pdfHasEmbeddedFonts() {
        val bytes = InvoiceFixtures.basic.toPdf()
        Loader.loadPDF(bytes).use { doc ->
            val page = doc.getPage(0)
            val fonts = page.resources.fontNames.toList()
            assertTrue(fonts.isNotEmpty(), "PDF should have embedded fonts")
        }
    }

    @Test
    fun pdfTextContainsKeyStrings() {
        val bytes = InvoiceFixtures.basic.toPdf()
        val text = Loader.loadPDF(bytes).use { PDFTextStripper().getText(it) }

        assertTrue("Acme" in text, "PDF should contain company name")
        assertTrue("Jane" in text, "PDF should contain client name")
        assertTrue("Web Development" in text, "PDF should contain line item")
    }

    @Test
    fun pdfAllFixturesValid() {
        InvoiceFixtures.all.forEachIndexed { i, doc ->
            val bytes = doc.toPdf()
            Loader.loadPDF(bytes).use {
                assertTrue(it.numberOfPages >= 1, "Fixture $i should have at least 1 page")
            }
        }
    }

    // --- HTML structural assertions ---

    @Test
    fun htmlHasTableForLineItems() {
        val html = InvoiceFixtures.basic.toHtml()
        assertTrue("<table" in html, "HTML should have tables for layout")
        assertTrue("<thead" in html || "<th" in html, "HTML should have table headers")
    }

    @Test
    fun htmlHasInlineStylesOnly() {
        val html = InvoiceFixtures.basic.toHtml()
        assertFalse("<style" in html, "HTML should not have <style> blocks (email compat)")
        assertTrue("style=\"" in html, "HTML should have inline styles")
    }

    @Test
    fun htmlImagesAreBase64DataUris() {
        // Create a fixture with an image to test this
        // For now, verify no external image references in basic fixtures
        val html = InvoiceFixtures.basic.toHtml()
        // Should not have external image src (only data: URIs allowed)
        assertFalse(Regex("""src="https?://""").containsMatchIn(html),
            "HTML images should be base64, not external URLs")
    }

    @Test
    fun htmlPaymentLinkIsAnchor() {
        val html = InvoiceFixtures.fullFeatured.toHtml()
        assertTrue("<a" in html, "Payment link should be an anchor")
        assertTrue("href=\"https://pay.acme.com/inv-0042\"" in html,
            "Payment link should have correct href")
    }

    @Test
    fun htmlAllFixturesValid() {
        InvoiceFixtures.all.forEachIndexed { i, doc ->
            val html = doc.toHtml()
            assertTrue(html.startsWith("<!DOCTYPE html>"), "Fixture $i should start with DOCTYPE")
            assertTrue("<html" in html, "Fixture $i should have <html> tag")
            assertTrue("</html>" in html, "Fixture $i should close <html> tag")
        }
    }
}
