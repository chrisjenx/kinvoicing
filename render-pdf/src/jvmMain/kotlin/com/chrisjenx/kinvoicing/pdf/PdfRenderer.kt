package com.chrisjenx.kinvoicing.pdf

import com.chrisjenx.compose2pdf.renderToPdf
import com.chrisjenx.kinvoicing.InvoiceDocument
import com.chrisjenx.kinvoicing.InvoiceRenderer
import com.chrisjenx.kinvoicing.compose.InvoiceSectionsContent
import com.chrisjenx.kinvoicing.compose.InvoiceStyleProvider
import com.chrisjenx.kinvoicing.currency
import java.io.OutputStream

/**
 * Renders an [InvoiceDocument] to PDF bytes via compose2pdf.
 *
 * Uses [InvoiceSectionsContent] for intelligent section grouping (e.g.,
 * adjacent BillFrom + BillTo rendered side-by-side). Each logical group is
 * a direct child of the `renderToPdf` lambda, enabling auto-pagination.
 */
public class PdfRenderer(
    private val config: PdfRenderConfig = PdfRenderConfig.Default,
) : InvoiceRenderer<ByteArray> {

    override fun render(document: InvoiceDocument): ByteArray {
        return renderToPdf(
            config = config.pageConfig,
            density = config.density,
            mode = config.renderMode,
            pagination = config.pagination,
        ) {
            InvoiceStyleProvider(document.style) {
                InvoiceSectionsContent(document.sections, document.currency)
            }
        }
    }

    /** Render the [document] and write the PDF bytes directly to [outputStream]. */
    public fun render(document: InvoiceDocument, outputStream: OutputStream) {
        outputStream.write(render(document))
    }
}

/**
 * Convenience extension to render an [InvoiceDocument] to PDF bytes.
 */
public fun InvoiceDocument.toPdf(
    config: PdfRenderConfig = PdfRenderConfig.Default,
): ByteArray = PdfRenderer(config).render(this)
