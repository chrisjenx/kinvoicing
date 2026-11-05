package com.chrisjenx.kinvoicing.pdf

import com.chrisjenx.compose2pdf.RenderMode
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

    /**
     * Guards the CMP 1.13 Skiko-registration regression ("No Compose UI graphics implementation is
     * registered (SkikoGraphics)"): every compose2pdf render mode must produce a valid, parseable PDF.
     * Covers every mode in one JVM so a mode that happens to register Skiko can't mask one that doesn't
     * (each mode is also asserted independently, so the first-run mode is exercised cold).
     */
    @Test
    fun everyRenderModeProducesValidPdf() {
        assertTrue(RenderMode.entries.isNotEmpty())
        for (mode in RenderMode.entries) {
            val bytes = PdfRenderer(PdfRenderConfig(renderMode = mode)).render(InvoiceFixtures.basic)
            assertTrue(bytes.size > 100, "$mode: PDF should have meaningful content (${bytes.size} bytes)")
            assertEquals("%PDF-", bytes.copyOfRange(0, 5).decodeToString(), "$mode: missing PDF header")
            Loader.loadPDF(bytes).use { doc ->
                assertTrue(doc.numberOfPages >= 1, "$mode: PDF should have at least 1 page")
                val page = doc.getPage(0)
                assertTrue(page.mediaBox.width > 0 && page.mediaBox.height > 0, "$mode: empty page box")
                if (mode == RenderMode.VECTOR) {
                    val text = PDFTextStripper().getText(doc).replace(" ", "")
                    assertTrue("INV-2026-0001" in text, "$mode: vector PDF should contain invoice number")
                } else {
                    assertTrue(page.resources.xObjectNames.any(), "$mode: raster PDF should embed an image")
                }
            }
        }
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
