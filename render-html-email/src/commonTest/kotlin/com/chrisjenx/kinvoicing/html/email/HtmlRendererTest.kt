package com.chrisjenx.kinvoicing.html.email

import com.chrisjenx.kinvoicing.*
import kotlin.test.*

class HtmlRendererTest {

    private val renderer = HtmlRenderer()

    @Test
    fun basicFixtureRendersWithoutError() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue(html.isNotBlank())
    }

    @Test
    fun allFixturesRenderWithoutError() {
        InvoiceFixtures.all.forEachIndexed { i, doc ->
            val html = renderer.render(doc)
            assertTrue(html.isNotBlank(), "Fixture $i produced empty HTML")
        }
    }

    @Test
    fun outputContainsDoctype() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue(html.startsWith("<!DOCTYPE html>"), "Should start with DOCTYPE")
    }

    @Test
    fun outputContainsHtmlAndBodyTags() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("<html" in html, "Missing <html> tag")
        assertTrue("<body" in html, "Missing <body> tag")
        assertTrue("</html>" in html, "Missing </html> tag")
        assertTrue("</body>" in html, "Missing </body> tag")
    }

    @Test
    fun noStyleBlocks() {
        InvoiceFixtures.all.forEach { doc ->
            val html = renderer.render(doc)
            assertFalse("<style" in html, "Email-safe HTML should not contain <style> blocks")
        }
    }

    @Test
    fun inlineStylesPresent() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("style=\"" in html, "Should contain inline styles")
    }

    @Test
    fun maxWidthConstraint() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("max-width" in html || "600" in html, "Should have max-width constraint for email")
    }

    @Test
    fun invoiceNumberAppears() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("INV-2026-0001" in html, "Invoice number should appear in output")
    }

    @Test
    fun billToNameAppears() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("Jane Smith" in html, "Bill-to name should appear")
    }

    @Test
    fun lineItemDescriptionsAppear() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("Web Development" in html)
        assertTrue("Design Services" in html)
        assertTrue("Hosting (Monthly)" in html)
    }

    @Test
    fun lineItemsTableExists() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("<table" in html, "Line items should be in a table")
        assertTrue("<thead" in html || "<th" in html, "Table should have headers")
        assertTrue("<tbody" in html || "<td" in html, "Table should have body rows")
    }

    @Test
    fun summaryTotalAppears() {
        val html = renderer.render(InvoiceFixtures.basic)
        // Basic fixture: 40*150 + 10*100 + 1*49.99 = 7049.99
        assertTrue("7,049.99" in html || "7049.99" in html, "Total should appear in output")
    }

    @Test
    fun footerNotesAppear() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("Thank you for your business!" in html)
    }

    @Test
    fun footerTermsAppear() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("Net 30" in html)
    }

    @Test
    fun paymentLinkRendersAsAnchor() {
        val html = renderer.render(InvoiceFixtures.fullFeatured)
        assertTrue("<a" in html && "https://pay.acme.com/inv-0042" in html,
            "Payment link should render as <a> tag")
    }

    @Test
    fun metaEntriesAppear() {
        val html = renderer.render(InvoiceFixtures.fullFeatured)
        assertTrue("PO Number" in html)
        assertTrue("PO-2026-1138" in html)
        assertTrue("Website Redesign" in html)
    }

    @Test
    fun brandingPrimaryNameAppears() {
        val html = renderer.render(InvoiceFixtures.basic)
        assertTrue("Acme Corp" in html)
    }

    @Test
    fun poweredByBrandingAppears() {
        val html = renderer.render(InvoiceFixtures.fullFeatured)
        assertTrue("Acme Payments" in html || "Powered by" in html,
            "Powered-by branding should appear")
    }

    @Test
    fun negativeAmountsDisplay() {
        val html = renderer.render(InvoiceFixtures.negativeValues)
        // Negative amounts should show as negative (minus sign or parentheses)
        assertTrue("-" in html || "(" in html,
            "Negative amounts should be visually indicated")
    }

    @Test
    fun longFixtureRendersAllItems() {
        val html = renderer.render(InvoiceFixtures.long)
        // Should contain first and last items
        assertTrue("Service item #1" in html)
        assertTrue("Service item #55" in html)
    }

    @Test
    fun minimalFixtureRendersGracefully() {
        val html = renderer.render(InvoiceFixtures.minimal)
        assertTrue("INV-MIN" in html)
        assertTrue("Service" in html)
    }

    @Test
    fun styledFixtureAppliesColors() {
        val html = renderer.render(InvoiceFixtures.styled)
        // Red primary color (#DC2626) should appear in inline styles
        assertTrue("DC2626" in html.uppercase() || "dc2626" in html.lowercase(),
            "Custom primary color should appear in styles")
    }

    @Test
    fun adjustmentsAppearInSummary() {
        val html = renderer.render(InvoiceFixtures.fullFeatured)
        assertTrue("Early payment" in html, "Discount label should appear")
        assertTrue("CO Sales Tax" in html, "Tax label should appear")
        assertTrue("Wire transfer fee" in html, "Fee label should appear")
    }

    @Test
    fun subItemsRender() {
        val html = renderer.render(InvoiceFixtures.fullFeatured)
        assertTrue("Senior engineer" in html, "Sub-items should render")
        assertTrue("Junior engineer" in html)
    }

    @Test
    fun configEmbedImagesDefault() {
        val config = HtmlRenderConfig()
        assertTrue(config.embedImages, "Default should embed images as base64")
    }

    @Test
    fun configIncludeDoctypeDefault() {
        val config = HtmlRenderConfig()
        assertTrue(config.includeDoctype, "Default should include DOCTYPE")
    }

    @Test
    fun extensionFunctionWorks() {
        val html = InvoiceFixtures.basic.toHtml()
        assertTrue(html.isNotBlank())
        assertTrue("INV-2026-0001" in html)
    }

    @Test
    fun customSectionTextLinkRendersAsInlineAnchor() {
        val doc = invoice {
            custom("test") { link("Home", "https://example.com") }
        }
        val html = doc.toHtml()
        assertTrue(
            "href=\"https://example.com\"" in html,
            "Expected an <a href=\"https://example.com\"> in TEXT-style output",
        )
        assertTrue(">Home<" in html, "Expected the link's display text to render")
        // TEXT-style links live inside a <div>, not a bulletproof <table><td>.
        val anchorIdx = html.indexOf("href=\"https://example.com\"")
        val priorTagOpen = html.lastIndexOf("<", anchorIdx)
        val priorTag = html.substring(priorTagOpen, anchorIdx)
        assertFalse("<td" in priorTag, "TEXT link should not be wrapped in a <td>")
    }

    @Test
    fun customSectionButtonRendersInsideTable() {
        val doc = invoice {
            custom("test") { button("Pay Now", "https://pay.example.com") }
        }
        val html = doc.toHtml()
        assertTrue(
            "href=\"https://pay.example.com\"" in html,
            "Expected an <a href=\"https://pay.example.com\"> in BUTTON-style output",
        )
        assertTrue(">Pay Now<" in html, "Expected the button's label to render")
        // Bulletproof BUTTON: anchor lives inside <td> within a <table>.
        val anchorIdx = html.indexOf("href=\"https://pay.example.com\"")
        val precedingHtml = html.substring(0, anchorIdx)
        val tableIdx = precedingHtml.lastIndexOf("<table")
        val tdIdx = precedingHtml.lastIndexOf("<td")
        assertTrue(tableIdx >= 0 && tdIdx > tableIdx, "BUTTON link should be wrapped in <table><tr><td>")
        // Confirm M3 visual defaults land on the <td>.
        val tdAttrs = precedingHtml.substring(tdIdx)
        assertTrue("border-radius:20px" in tdAttrs, "Expected border-radius:20px on button <td>")
        assertTrue("padding:10px 24px" in tdAttrs, "Expected padding:10px 24px on button <td>")
    }
}
