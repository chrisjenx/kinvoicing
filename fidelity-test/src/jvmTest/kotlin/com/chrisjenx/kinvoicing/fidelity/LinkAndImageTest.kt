package com.chrisjenx.kinvoicing.fidelity

import com.chrisjenx.kinvoicing.InvoiceFixtures
import com.chrisjenx.kinvoicing.html.email.toHtml
import com.chrisjenx.kinvoicing.pdf.toPdf
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import org.apache.pdfbox.text.PDFTextStripper
import org.jsoup.Jsoup
import kotlin.test.*

/**
 * Cross-renderer link and image tests.
 *
 * Validates that links (website, email, phone, payment) and images (logo, custom)
 * render correctly in both HTML email and PDF output.
 */
class LinkAndImageTest {

    private val fixture = InvoiceFixtures.linksAndImages
    private val payButton = InvoiceFixtures.payButton

    // ── HTML Email Link Tests ──

    @Test
    fun htmlEmail_brandWebsiteIsLink() {
        val doc = Jsoup.parse(fixture.toHtml())
        val link = doc.select("a[href='https://example.com']")
        assertFalse(link.isEmpty(), "Brand website should be an <a> tag")
    }

    @Test
    fun htmlEmail_brandEmailIsMailtoLink() {
        val doc = Jsoup.parse(fixture.toHtml())
        val link = doc.select("a[href='mailto:brand@example.com']")
        assertFalse(link.isEmpty(), "Brand email should be a mailto: link")
    }

    @Test
    fun htmlEmail_brandPhoneIsTelLink() {
        val doc = Jsoup.parse(fixture.toHtml())
        val link = doc.select("a[href^='tel:']")
        assertFalse(link.isEmpty(), "Brand phone should be a tel: link")
    }

    @Test
    fun htmlEmail_billFromEmailIsMailtoLink() {
        val doc = Jsoup.parse(fixture.toHtml())
        val links = doc.select("a[href='mailto:from@example.com']")
        assertFalse(links.isEmpty(), "BillFrom email should be a mailto: link")
    }

    @Test
    fun htmlEmail_billFromPhoneIsTelLink() {
        val doc = Jsoup.parse(fixture.toHtml())
        val links = doc.select("a[href='tel:+1-555-0200']")
        assertFalse(links.isEmpty(), "BillFrom phone should be a tel: link")
    }

    @Test
    fun htmlEmail_billToEmailIsMailtoLink() {
        val doc = Jsoup.parse(fixture.toHtml())
        val links = doc.select("a[href='mailto:to@example.com']")
        assertFalse(links.isEmpty(), "BillTo email should be a mailto: link")
    }

    @Test
    fun htmlEmail_billToPhoneIsTelLink() {
        val doc = Jsoup.parse(fixture.toHtml())
        val links = doc.select("a[href='tel:+1-555-0300']")
        assertFalse(links.isEmpty(), "BillTo phone should be a tel: link")
    }

    @Test
    fun htmlEmail_paymentLinkIsClickable() {
        val doc = Jsoup.parse(fixture.toHtml())
        val link = doc.select("a[href='https://pay.example.com/inv-001']")
        assertFalse(link.isEmpty(), "Payment link should be an <a> tag")
    }

    // ── HTML Email Image Tests ──

    @Test
    fun htmlEmail_logoRendersAsImage() {
        val doc = Jsoup.parse(fixture.toHtml())
        val imgs = doc.select("img[src^='data:image/png;base64,']")
        assertTrue(imgs.isNotEmpty(), "Logo should render as base64 <img> tag")
    }

    @Test
    fun htmlEmail_customImageRendersAsImage() {
        val doc = Jsoup.parse(fixture.toHtml())
        val imgs = doc.select("img[src^='data:image/png;base64,']")
        assertTrue(imgs.size >= 2, "Both logo and custom image should render as <img> tags, found ${imgs.size}")
    }

    @Test
    fun htmlEmail_noExternalImageUrls() {
        val doc = Jsoup.parse(fixture.toHtml())
        val externalImgs = doc.select("img[src^='http']")
        assertTrue(externalImgs.isEmpty(), "Images should be base64 data URIs, not external URLs")
    }

    // ── HTML Email Named Link Tests ──

    @Test
    fun htmlEmail_customNamedLinkRendersWithText() {
        val doc = Jsoup.parse(fixture.toHtml())
        val link = doc.select("a[href='https://example.com/custom-link']")
        assertFalse(link.isEmpty(), "Custom named link should render as <a> tag")
        assertEquals("Visit Our Website", link.first()!!.text(), "Link text should be the display name, not the URL")
    }

    // ── PDF Link Tests ──

    @Test
    fun pdf_brandWebsiteIsAnnotation() {
        val uris = extractPdfLinkUris(fixture.toPdf())
        assertTrue(
            "https://example.com" in uris,
            "PDF should have brand website link annotation, found: $uris",
        )
    }

    @Test
    fun pdf_paymentLinkIsAnnotation() {
        val uris = extractPdfLinkUris(fixture.toPdf())
        assertTrue(
            "https://pay.example.com/inv-001" in uris,
            "PDF should have payment link annotation, found: $uris",
        )
    }

    @Test
    fun pdf_emailLinksAreAnnotations() {
        val uris = extractPdfLinkUris(fixture.toPdf())
        assertTrue(
            uris.any { it.startsWith("mailto:") },
            "PDF should have mailto: annotations, found: $uris",
        )
    }

    @Test
    fun pdf_phoneLinksAreAnnotations() {
        val uris = extractPdfLinkUris(fixture.toPdf())
        assertTrue(
            uris.any { it.startsWith("tel:") },
            "PDF should have tel: annotations, found: $uris",
        )
    }

    @Test
    fun pdf_customNamedLinkIsAnnotation() {
        val uris = extractPdfLinkUris(fixture.toPdf())
        assertTrue(
            "https://example.com/custom-link" in uris,
            "PDF should have custom named link annotation, found: $uris",
        )
    }

    @Test
    fun pdf_logoIsNotPlaceholder() {
        val text = Loader.loadPDF(fixture.toPdf()).use { PDFTextStripper().getText(it) }
        assertFalse("[Image:" in text, "Logo should not render as placeholder text, found: $text")
    }

    @Test
    fun pdf_customImageIsNotPlaceholder() {
        val text = Loader.loadPDF(fixture.toPdf()).use { PDFTextStripper().getText(it) }
        assertFalse(
            "[Image: image/png]" in text,
            "Custom image should not render as placeholder text",
        )
    }

    // ── Cross-renderer consistency ──

    @Test
    fun allRenderersProduceSameLinkUrls() {
        val expectedUrls = setOf(
            "https://example.com",
            "mailto:brand@example.com",
            "tel:+1-555-0100",
            "mailto:from@example.com",
            "tel:+1-555-0200",
            "mailto:to@example.com",
            "tel:+1-555-0300",
            "https://pay.example.com/inv-001",
            "https://example.com/custom-link",
        )

        // HTML email
        val htmlDoc = Jsoup.parse(fixture.toHtml())
        val htmlHrefs = htmlDoc.select("a[href]").map { it.attr("href") }.toSet()
        val missingFromHtml = expectedUrls - htmlHrefs
        assertTrue(missingFromHtml.isEmpty(), "HTML email missing links: $missingFromHtml")

        // PDF
        val pdfUris = extractPdfLinkUris(fixture.toPdf()).toSet()
        val missingFromPdf = expectedUrls - pdfUris
        assertTrue(missingFromPdf.isEmpty(), "PDF missing link annotations: $missingFromPdf")
    }

    // ── BUTTON-style and rich-notes cross-renderer assertions ──

    @Test
    fun pdf_payButtonProducesAnnotation() {
        val uris = extractPdfLinkUris(payButton.toPdf())
        assertTrue(
            "https://pay.acme.com/inv-paybtn" in uris,
            "PDF should have BUTTON-style payment link annotation, found: $uris",
        )
    }

    @Test
    fun htmlEmail_payButtonRendersInsideTable() {
        val html = payButton.toHtml()
        val anchorIdx = html.indexOf("href=\"https://pay.acme.com/inv-paybtn\"")
        assertTrue(anchorIdx >= 0, "Expected payment link <a> in BUTTON-style HTML")
        val precedingHtml = html.substring(0, anchorIdx)
        val tableIdx = precedingHtml.lastIndexOf("<table")
        val tdIdx = precedingHtml.lastIndexOf("<td")
        assertTrue(
            tableIdx >= 0 && tdIdx > tableIdx,
            "BUTTON-style payment link should be wrapped in <table><tr><td>",
        )
    }

    @Test
    fun pdf_paymentNotesEmbeddedLinkProducesAnnotation() {
        val uris = extractPdfLinkUris(payButton.toPdf())
        assertTrue(
            "https://acme.com/wire-policy" in uris,
            "PDF should annotate inline link in PaymentInfo.notes, found: $uris",
        )
    }

    @Test
    fun htmlEmail_paymentNotesEmbeddedLinkRenders() {
        val html = payButton.toHtml()
        assertTrue(
            "href=\"https://acme.com/wire-policy\"" in html,
            "PaymentInfo.notes inline link should render as <a href>",
        )
    }

    @Test
    fun pdf_footerEmbeddedLinkProducesAnnotation() {
        val uris = extractPdfLinkUris(payButton.toPdf())
        assertTrue(
            "https://acme.com/terms" in uris,
            "PDF should annotate inline link in Footer.notes, found: $uris",
        )
        assertTrue(
            "mailto:billing@acme.com" in uris,
            "PDF should annotate mailto: link in Footer.notes, found: $uris",
        )
    }

    @Test
    fun htmlEmail_footerEmbeddedLinkRenders() {
        val html = payButton.toHtml()
        assertTrue("href=\"https://acme.com/terms\"" in html, "Footer.notes link should render")
        assertTrue("href=\"mailto:billing@acme.com\"" in html, "Footer.notes mailto: link should render")
    }

    // ── Helper ──

    private fun extractPdfLinkUris(pdfBytes: ByteArray): List<String> {
        return Loader.loadPDF(pdfBytes).use { doc ->
            (0 until doc.numberOfPages).flatMap { pageIdx ->
                doc.getPage(pageIdx).annotations
                    .filterIsInstance<PDAnnotationLink>()
                    .mapNotNull { link ->
                        val action = link.action
                        if (action is PDActionURI) action.uri else null
                    }
            }
        }
    }
}
