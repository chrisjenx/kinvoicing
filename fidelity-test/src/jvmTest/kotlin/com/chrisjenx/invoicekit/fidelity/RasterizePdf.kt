package com.chrisjenx.invoicekit.fidelity

import com.chrisjenx.invoicekit.InvoiceDocument
import com.chrisjenx.invoicekit.pdf.toPdf
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage

/**
 * Renders an InvoiceDocument to PDF via compose2pdf, then rasterizes with PDFBox.
 */
internal object RasterizePdf {

    fun rasterize(
        document: InvoiceDocument,
        dpi: Float = 144f,
    ): List<BufferedImage> {
        val pdfBytes = document.toPdf()
        return rasterizeBytes(pdfBytes, dpi)
    }

    fun rasterizeBytes(pdfBytes: ByteArray, dpi: Float = 144f): List<BufferedImage> {
        val doc = Loader.loadPDF(pdfBytes)
        return doc.use {
            val renderer = PDFRenderer(it)
            (0 until it.numberOfPages).map { pageIdx ->
                renderer.renderImageWithDPI(pageIdx, dpi)
            }
        }
    }
}
