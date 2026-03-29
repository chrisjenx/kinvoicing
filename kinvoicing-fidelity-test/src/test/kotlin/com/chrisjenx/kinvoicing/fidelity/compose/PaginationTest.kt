package com.chrisjenx.kinvoicing.fidelity.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.chrisjenx.compose2pdf.PdfPageConfig
import com.chrisjenx.compose2pdf.PdfPagination
import com.chrisjenx.compose2pdf.RenderMode
import com.chrisjenx.compose2pdf.renderToPdf
import com.chrisjenx.kinvoicing.compose.InvoiceSectionContent
import com.chrisjenx.kinvoicing.compose.InvoiceStyleProvider
import com.chrisjenx.kinvoicing.currency
import com.chrisjenx.kinvoicing.examples.InvoiceExamples
import org.apache.pdfbox.Loader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaginationTest {

    private val config = PdfPageConfig.A4
    private val density = Density(2f)

    @Test
    fun `single-page invoice produces 1 page`() {
        val doc = InvoiceExamples.basic
        val pdfBytes = renderPaginated(doc)

        Loader.loadPDF(pdfBytes).use { pdf ->
            assertEquals(1, pdf.numberOfPages, "Basic invoice should fit on 1 page")
        }
    }

    @Test
    fun `long invoice paginates across multiple pages`() {
        val doc = InvoiceExamples.pagination
        val pdfBytes = renderPaginated(doc)

        Loader.loadPDF(pdfBytes).use { pdf ->
            assertTrue(
                pdf.numberOfPages > 1,
                "50-item invoice should span multiple pages, got ${pdf.numberOfPages}",
            )
        }
    }

    @Test
    fun `all pages have content`() {
        val doc = InvoiceExamples.pagination
        val pdfBytes = renderPaginated(doc)

        Loader.loadPDF(pdfBytes).use { pdf ->
            val renderer = org.apache.pdfbox.rendering.PDFRenderer(pdf)
            for (i in 0 until pdf.numberOfPages) {
                val image = renderer.renderImageWithDPI(i, 72f)
                // Each page should have non-white pixels (actual content)
                val hasContent = (0 until image.width).any { x ->
                    (0 until image.height).any { y ->
                        val rgb = image.getRGB(x, y) and 0xFFFFFF
                        rgb != 0xFFFFFF
                    }
                }
                assertTrue(hasContent, "Page ${i + 1} should have visible content")
            }
        }
    }

    private fun renderPaginated(doc: com.chrisjenx.kinvoicing.InvoiceDocument): ByteArray {
        return renderToPdf(
            config = config,
            density = density,
            mode = RenderMode.VECTOR,
            pagination = PdfPagination.AUTO,
        ) {
            InvoiceStyleProvider(doc.style) {
                for (section in doc.sections) {
                    InvoiceSectionContent(section, doc.currency)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
