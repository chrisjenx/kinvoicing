package com.chrisjenx.kinvoicing.pdf

import com.chrisjenx.kinvoicing.InvoiceFixtures
import com.chrisjenx.kinvoicing.InvoiceSection
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import kotlin.test.*

class PdfRendererTest {

    private val renderer = PdfRenderer()

    @Test
    fun basicFixtureRendersNonEmptyPdf() {
        val bytes = renderer.render(InvoiceFixtures.basic)
        assertTrue(bytes.isNotEmpty(), "PDF should not be empty")
        assertTrue(bytes.size > 100, "PDF should have meaningful content")
    }

    @Test
    fun outputStartsWithPdfMagicBytes() {
        val bytes = renderer.render(InvoiceFixtures.basic)
        assertEquals('%'.code.toByte(), bytes[0])
        assertEquals('P'.code.toByte(), bytes[1])
        assertEquals('D'.code.toByte(), bytes[2])
        assertEquals('F'.code.toByte(), bytes[3])
    }

    @Test
    fun allFixturesRenderWithoutError() {
        InvoiceFixtures.all.forEachIndexed { i, doc ->
            val bytes = renderer.render(doc)
            assertTrue(bytes.isNotEmpty(), "Fixture $i produced empty PDF")
        }
    }

    @Test
    fun pdfCanBeParsedByPdfBox() {
        val bytes = renderer.render(InvoiceFixtures.basic)
        val doc = Loader.loadPDF(bytes)
        doc.use {
            assertTrue(it.numberOfPages >= 1, "PDF should have at least 1 page")
        }
    }

    @Test
    fun textExtractionContainsInvoiceNumber() {
        val bytes = renderer.render(InvoiceFixtures.basic)
        val text = extractText(bytes)
        // PDF text extraction may insert spaces around hyphens
        val normalized = text.replace(" ", "")
        assertTrue("INV-2026-0001" in normalized, "PDF text should contain invoice number. Got: $text")
    }

    @Test
    fun textExtractionContainsBillToName() {
        val bytes = renderer.render(InvoiceFixtures.basic)
        val text = extractText(bytes)
        assertTrue("Jane Smith" in text, "PDF text should contain bill-to name")
    }

    @Test
    fun textExtractionContainsLineItemDescriptions() {
        val bytes = renderer.render(InvoiceFixtures.basic)
        val text = extractText(bytes)
        assertTrue("Web Development" in text, "PDF should contain line item")
    }

    @Test
    fun embeddedFontsPresent() {
        val bytes = renderer.render(InvoiceFixtures.basic)
        val doc = Loader.loadPDF(bytes)
        doc.use {
            val page = it.getPage(0)
            val fonts = page.resources.fontNames
            assertTrue(fonts.toList().isNotEmpty(), "PDF should have embedded fonts")
        }
    }

    @Test
    fun longFixtureProducesMultiplePages() {
        val bytes = renderer.render(InvoiceFixtures.long)
        val doc = Loader.loadPDF(bytes)
        doc.use {
            assertTrue(it.numberOfPages > 1, "Long fixture should span multiple pages, got ${it.numberOfPages}")
        }
    }

    @Test
    fun paymentLinkAnnotationPresent() {
        val bytes = renderer.render(InvoiceFixtures.fullFeatured)
        val text = extractText(bytes)
        assertTrue(
            "pay.acme.com" in text || "Pay Online" in text,
            "PDF should contain payment link text"
        )
    }

    @Test
    fun extensionFunctionWorks() {
        val bytes = InvoiceFixtures.basic.toPdf()
        assertTrue(bytes.isNotEmpty())
        assertEquals('%'.code.toByte(), bytes[0])
    }

    @Test
    fun outputStreamRenderWorks() {
        val baos = java.io.ByteArrayOutputStream()
        renderer.render(InvoiceFixtures.basic, baos)
        val bytes = baos.toByteArray()
        assertTrue(bytes.isNotEmpty())
        assertEquals('%'.code.toByte(), bytes[0])
    }

    private fun extractText(pdfBytes: ByteArray): String {
        return Loader.loadPDF(pdfBytes).use { doc ->
            PDFTextStripper().getText(doc)
        }
    }
}
