package com.chrisjenx.invoicekit.pdf

import com.chrisjenx.compose2pdf.renderToPdf
import com.chrisjenx.invoicekit.InvoiceDocument
import com.chrisjenx.invoicekit.InvoiceRenderer
import com.chrisjenx.invoicekit.compose.InvoiceContent
import java.io.OutputStream

/**
 * Renders an [InvoiceDocument] to PDF bytes by feeding the shared [InvoiceContent]
 * composable through compose2pdf's [renderToPdf].
 *
 * PDF matches the Compose preview by construction — same composable, same layout.
 */
public class PdfRenderer(
    private val config: PdfRenderConfig = PdfRenderConfig.Default,
) : InvoiceRenderer<ByteArray> {

    override fun render(document: InvoiceDocument): ByteArray {
        return renderToPdf(config = config.pageConfig, density = config.density, mode = config.renderMode) {
            InvoiceContent(document)
        }
    }

    /** Render the [document] and write the PDF bytes directly to [outputStream]. */
    public fun render(document: InvoiceDocument, outputStream: OutputStream) {
        val bytes = render(document)
        outputStream.write(bytes)
    }
}

/**
 * Convenience extension to render an [InvoiceDocument] to PDF bytes.
 */
public fun InvoiceDocument.toPdf(
    config: PdfRenderConfig = PdfRenderConfig.Default,
): ByteArray {
    return PdfRenderer(config).render(this)
}
